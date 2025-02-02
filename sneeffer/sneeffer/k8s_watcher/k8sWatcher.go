package k8s_watcher

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"github.com/docker/docker/api/types"
	"github.com/kubescape/sneeffer/internal/logger"
	"github.com/kubescape/sneeffer/sneeffer/DB"
	"github.com/kubescape/sneeffer/sneeffer/aggregator"
	"github.com/kubescape/sneeffer/sneeffer/container_profiling"
	global_data "github.com/kubescape/sneeffer/sneeffer/global_data/k8s"
	"github.com/kubescape/sneeffer/sneeffer/sbom"
	"github.com/kubescape/sneeffer/sneeffer/vuln"
	apiextension "k8s.io/apiextensions-apiserver/pkg/client/clientset/clientset"

	"github.com/kubescape/k8s-interface/cloudsupport"
	"github.com/kubescape/sneeffer/internal/config"
	core "k8s.io/api/core/v1"
	v1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/watch"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/clientcmd"
)

const (
	STEP_GET_SBOM           = "STEP_GET_SBOM"
	STEP_GET_UNFILTER_VULNS = "STEP_GET_UNFILTER_VULNS"
	STEP_AGGREGATOR         = "STEP_AGGREGATOR"
)

type k8sTripeletIdentity struct {
	namespace       string
	k8sAncestorType string
	imageName       string
}

type watchedContainer struct {
	containerAggregator       *aggregator.Aggregator
	containerAggregatorStatus bool
	sbomObject                *sbom.SbomObject
	vulnObject                *vuln.VulnObject
	imageID                   string
	podName                   string
	snifferTimer              *time.Timer
	k8sIdentity               k8sTripeletIdentity
	syncChannel               map[string]chan error
}

type afterTimerActionsData struct {
	containerID string
	service     string
}

type ContainerWatcher struct {
	k8sClient                *kubernetes.Clientset
	watchedContainers        map[string]*watchedContainer
	nodeName                 string
	afterTimerActionsChannel chan afterTimerActionsData
}

var containerWatcher *ContainerWatcher

func getMyNode(clientset *kubernetes.Clientset) (string, error) {
	nodeName, exist := os.LookupEnv("myNode")
	if exist {
		return nodeName, nil
	}
	return "", fmt.Errorf("getMyNode: the env var myNode is missing")
}

func CreateContainerWatcher() (*ContainerWatcher, error) {
	var err error
	var home string
	var exist bool
	var configPath string

	restConfig, err := rest.InClusterConfig()
	if err != nil {
		home, exist = os.LookupEnv("HOME")
		if !exist {
			home = "/root"
		}
		configPath = filepath.Join(home, ".kube", "config")
		restConfig, err = clientcmd.BuildConfigFromFlags("", configPath)
		if err != nil {
			return nil, err
		}
	}

	clientset, err := kubernetes.NewForConfig(restConfig)
	if err != nil {
		return nil, err
	}

	nodeName, err := getMyNode(clientset)
	if err != nil {
		return nil, err
	}

	containerWatcher = &ContainerWatcher{
		k8sClient:                clientset,
		watchedContainers:        map[string]*watchedContainer{},
		nodeName:                 nodeName,
		afterTimerActionsChannel: make(chan afterTimerActionsData, 10),
	}
	return containerWatcher, nil
}

func (containerWatcher *ContainerWatcher) isContainerWatched(containerID string) bool {
	_, exist := containerWatcher.watchedContainers[containerID]
	return exist
}

func getShortContainerID(containerID string) string {
	cont := strings.Split(containerID, "://")
	return cont[1][:12]
}

func getImageID(imageID string) string {
	splitted := strings.Split(imageID, "://")
	if len(splitted) > 1 {
		return splitted[1]
	}
	return splitted[0]
}

func getK8SResourceName(containerData *watchedContainer) string {
	return "namespace-" + containerData.k8sIdentity.namespace + "." + containerData.k8sIdentity.k8sAncestorType + ".imagename-" + containerData.k8sIdentity.imageName
}

func (containerWatcher *ContainerWatcher) afterTimerActions() error {
	var err error
	var syscallList []string

	for {
		afterTimerActionsData := <-containerWatcher.afterTimerActionsChannel
		containerData := containerWatcher.watchedContainers[afterTimerActionsData.containerID]
		resourceName := getK8SResourceName(containerData)

		if config.IsRelaventCVEServiceEnabled() && afterTimerActionsData.service == config.RELAVENT_CVES_SERVICE {
			fileList := containerData.containerAggregator.GetContainerRealtimeFileList()
			if err = <-containerData.syncChannel[STEP_GET_SBOM]; err != nil {
				logger.Print(logger.ERROR, false, "afterTimerActions: failed to get sbom with err %v\n", err)
				continue
			}
			err = containerData.sbomObject.FilterSbom(fileList)
			if err != nil {
				logger.Print(logger.ERROR, false, "afterTimerActions: failed to filter sbom with err %v\n", err)
				continue
			}

			if err = <-containerData.syncChannel[STEP_GET_UNFILTER_VULNS]; err != nil {
				logger.Print(logger.ERROR, false, "afterTimerActions: failed to get unfilter vulns with err %v\n", err)
				continue
			}
			err = containerData.vulnObject.GetFilterVulnerabilities()
			if err != nil {
				logger.Print(logger.ERROR, false, "afterTimerActions: failed to get filter vulns with err %v\n", err)
				continue
			}
		}

		if config.IsContainerProfilingServiceEnabled() && afterTimerActionsData.service == config.CONTAINER_PROFILING_SERVICE {
			syscallList = containerData.containerAggregator.GetContainerRealtimeSyscalls()
		}

		err = DB.SetDataInDB(containerData.vulnObject.GetProcessedData(), container_profiling.CreateSeccompProfile(syscallList), resourceName, afterTimerActionsData.service)
		if err != nil {
			logger.Print(logger.ERROR, false, "afterTimerActions: failed to set data in the DB with err %v\n", err)
			continue
		}
	}
}

func (containerWatcher *ContainerWatcher) createTimer() *time.Timer {
	snifferTime, exist := os.LookupEnv("snifferTime")
	if !exist {
		logger.Print(logger.WARNING, false, "startTimer: snifferTime env var is no exist\n")
		logger.Print(logger.WARNING, false, "startTimer: sniffing container time will be 5 minutes\n")
		snifferTime = "5"
	}

	sniffTime, err := strconv.Atoi(snifferTime)
	if err != nil {
		logger.Print(logger.ERROR, false, "fail to convert string sniffertimer time to int with err %v\n", err)
		return nil
	}
	timer := time.NewTimer(time.Duration(sniffTime) * time.Minute)
	return timer
}

func (containerWatcher *ContainerWatcher) watchAggregatorStatus(containerID string) {
	containerData := containerWatcher.watchedContainers[containerID]

	err := <-containerData.syncChannel[STEP_AGGREGATOR]
	if err.Error() == "drop event occured\n" {
		containerData.containerAggregatorStatus = false
		containerData.snifferTimer.Stop()
	}
}

func (containerWatcher *ContainerWatcher) startTimer(containerID string) {
	var err error
	containerData := containerWatcher.watchedContainers[containerID]
	resourceName := getK8SResourceName(containerWatcher.watchedContainers[containerID])
	select {
	case <-containerData.snifferTimer.C:
		logger.Print(logger.INFO, false, "stop sniffing on containerID %s in k8s resource %s\n", getShortContainerID(containerID), resourceName)
		containerData.containerAggregator.StopAggregate()
		if config.IsContainerProfilingServiceEnabled() {
			containerWatcher.afterTimerActionsChannel <- afterTimerActionsData{
				containerID: containerID,
				service:     config.CONTAINER_PROFILING_SERVICE,
			}
		}
		if config.IsRelaventCVEServiceEnabled() {
			containerWatcher.afterTimerActionsChannel <- afterTimerActionsData{
				containerID: containerID,
				service:     config.RELAVENT_CVES_SERVICE,
			}
		}
	case err = <-containerData.syncChannel[STEP_AGGREGATOR]:
		if err.Error() == "drop event occured\n" {
			containerData.containerAggregatorStatus = false
			containerData.snifferTimer.Stop()
			logger.Print(logger.ERROR, false, "stop sniffing on containerID %s in k8s resource %s with failed status since we missed some events from the kernel", getShortContainerID(containerID), resourceName)
		}
	}
}

func (containerWatcher *ContainerWatcher) StartFindRelaventCVEsPrerequisites(containerID string) {
	containerData := containerWatcher.watchedContainers[containerID]

	/*phase 1: create sbom to image */
	go containerData.sbomObject.CreateSbomUnfilter(containerData.syncChannel[STEP_GET_SBOM])

	/*phase 2: create sbom to image */
	go containerData.vulnObject.GetImageVulnerabilities(containerData.syncChannel[STEP_GET_UNFILTER_VULNS])
}

func (containerWatcher *ContainerWatcher) StartContainerProfilingPrerequisites(containerID string) {
	return
}

func connectToK8sApiextension() (*apiextension.Clientset, error) {
	var err error
	var home string
	var exist bool
	var configPath string

	restConfig, err := rest.InClusterConfig()
	if err != nil {
		logger.Print(logger.DEBUG, false, "InClusterConfig err %v\n", err)
		home, exist = os.LookupEnv("HOME")
		if !exist {
			home = "/root"
		}
		configPath = filepath.Join(home, ".kube", "config")
		restConfig, err = clientcmd.BuildConfigFromFlags("", configPath)
		if err != nil {
			return nil, err
		}
	}

	clientset, err := apiextension.NewForConfig(restConfig)
	if err != nil {
		return nil, err
	}

	return clientset, nil
}

func (containerWatcher *ContainerWatcher) GetOwnerData(name string, kind string, namespace string) *[]string {
	switch kind {
	case "Deployment":
		options := v1.GetOptions{}
		depDet, err := containerWatcher.k8sClient.AppsV1().Deployments(namespace).Get(global_data.GlobalHTTPContext, name, options)
		if err != nil {
			logger.Print(logger.WARNING, false, "GetOwnerData Deployments: %s\n", err.Error())
			return nil
		}
		return &[]string{kind, depDet.GetName()}
	case "DaemonSet":
		options := v1.GetOptions{}
		daemSetDet, err := containerWatcher.k8sClient.AppsV1().DaemonSets(namespace).Get(global_data.GlobalHTTPContext, name, options)
		if err != nil {
			logger.Print(logger.WARNING, false, "GetOwnerData DaemonSets: %s\n", err.Error())
			return nil
		}
		return &[]string{kind, daemSetDet.GetName()}
	case "StatefulSet":
		options := v1.GetOptions{}
		statSetDet, err := containerWatcher.k8sClient.AppsV1().StatefulSets(namespace).Get(global_data.GlobalHTTPContext, name, options)
		if err != nil {
			logger.Print(logger.WARNING, false, "GetOwnerData StatefulSets: %s\n", err.Error())
			return nil
		}
		return &[]string{kind, statSetDet.GetName()}
	case "Job":
		options := v1.GetOptions{}
		jobDet, err := containerWatcher.k8sClient.BatchV1().Jobs(namespace).Get(global_data.GlobalHTTPContext, name, options)
		if err != nil {
			logger.Print(logger.WARNING, false, "GetOwnerData Jobs: %s\n", err.Error())
			return nil
		}
		if len(jobDet.GetObjectMeta().GetOwnerReferences()) > 0 {
			return containerWatcher.GetOwnerData(jobDet.GetObjectMeta().GetOwnerReferences()[0].Name, jobDet.GetObjectMeta().GetOwnerReferences()[0].Kind, jobDet.GetNamespace())
		}
		return &[]string{kind, jobDet.GetName()}
	case "CronJob":
		options := v1.GetOptions{}
		cronJobDet, err := containerWatcher.k8sClient.BatchV1beta1().CronJobs(namespace).Get(global_data.GlobalHTTPContext, name, options)
		if err != nil {
			logger.Print(logger.WARNING, false, "GetOwnerData CronJobs: %s\n", err.Error())
			return nil
		}
		return &[]string{kind, cronJobDet.GetName()}
	case "Pod", "Node":
		return &[]string{kind, name}
	case "ReplicaSet":
		repItem, err := containerWatcher.k8sClient.AppsV1().ReplicaSets(namespace).Get(global_data.GlobalHTTPContext, name, v1.GetOptions{})
		if err != nil {
			logger.Print(logger.WARNING, false, "GetOwnerData Pods: %s\n", err.Error())
			return nil
		}
		return containerWatcher.GetOwnerData(repItem.GetObjectMeta().GetOwnerReferences()[0].Name, repItem.GetObjectMeta().GetOwnerReferences()[0].Kind, repItem.GetNamespace())
	default:
		client, err := connectToK8sApiextension()
		if err != nil {
			logger.Print(logger.WARNING, false, "GetOwnerData connectToK8sApiextension: %s\n", err.Error())
			return nil
		}
		crds, err := client.ApiextensionsV1().CustomResourceDefinitions().List(context.Background(), v1.ListOptions{})
		if err != nil {
			logger.Print(logger.WARNING, false, "GetOwnerData CustomResourceDefinitions: %s\n", err.Error())
			return nil
		}
		for crdIdx := range crds.Items {
			if crds.Items[crdIdx].Status.AcceptedNames.Kind == kind {
				return &[]string{crds.Items[crdIdx].GetObjectKind().GroupVersionKind().Kind, crds.Items[crdIdx].GetName()}
			}
		}
		logger.Print(logger.WARNING, false, "GetOwnerData unknown k8s type: %s\n", err.Error())
		return nil
	}
}

func (containerWatcher *ContainerWatcher) getK8SIdentityTripplet(pod *core.Pod, imageName string) k8sTripeletIdentity {
	var k8sAncestor *[]string

	if len(pod.GetOwnerReferences()) != 0 {
		k8sAncestor = containerWatcher.GetOwnerData(pod.GetOwnerReferences()[0].Name, pod.GetOwnerReferences()[0].Kind, pod.GetNamespace())
		if k8sAncestor == nil {
			k8sAncestor = &[]string{"unknownType", "unknownName"}
		}
	} else {
		k8sAncestor = &[]string{"Pod", pod.GetName()}
	}

	logger.Print(logger.DEBUG, false, "getK8SIdentityTripplet: namespace-%s/%s/imageName-%s\n", pod.Namespace, (*k8sAncestor)[0]+"-"+(*k8sAncestor)[1], imageName)
	return k8sTripeletIdentity{
		namespace:       pod.Namespace,
		k8sAncestorType: strings.ToLower((*k8sAncestor)[0]) + "-" + (*k8sAncestor)[1],
		imageName:       imageName,
	}
}

func getImageCredentialslist(pod *core.Pod, imageID string) []types.AuthConfig {
	secrets, err := cloudsupport.GetImageRegistryCredentials(imageID, pod)
	if err != nil {
		logger.Print(logger.WARNING, false, "StartWatchingOnNewContainers GetImageRegistryCredentialsfaled with err %v\n", err)
	}
	credentialslist := make([]types.AuthConfig, 0)
	for secretName := range secrets {
		secret := secrets[secretName]
		if secret.Username != "" && secret.Password != "" {
			secret.Auth = ""
		}
		credentialslist = append(credentialslist, secret)
	}

	return credentialslist
}

func (containerWatcher *ContainerWatcher) StartWatchingOnNewContainers() error {
	logger.Print(logger.INFO, false, "sneeffer is ready to watch over node %s\n", containerWatcher.nodeName)
	go containerWatcher.afterTimerActions()

	for {
		watcher, err := containerWatcher.k8sClient.CoreV1().Pods("").Watch(global_data.GlobalHTTPContext, v1.ListOptions{})
		if err != nil {
			return err
		}
		for {
			event, chanActive := <-watcher.ResultChan()
			if !chanActive {
				watcher.Stop()
				break
			}
			if event.Type == watch.Error {
				logger.Print(logger.ERROR, false, "StartWatchingOnNewContainers: watch event failed with error: %v\n", event.Object)
				watcher.Stop()
				break
			}
			pod, ok := event.Object.(*core.Pod)
			if !ok {
				continue
			}
			if pod.Spec.NodeName != containerWatcher.nodeName {
				continue
			}
			switch event.Type {
			case watch.Added:
				for i := range pod.Status.ContainerStatuses {
					if *pod.Status.ContainerStatuses[i].Started {
						if strings.Contains(pod.GetName(), "kubescape-sneeffer") {
							config.SetMyContainerID(getShortContainerID(pod.Status.ContainerStatuses[i].ContainerID))
						}
					}
				}
			case watch.Modified:
				for i := range pod.Status.ContainerStatuses {
					if *pod.Status.ContainerStatuses[i].Started && !containerWatcher.isContainerWatched(pod.Status.ContainerStatuses[i].ContainerID) {
						if strings.Contains(pod.GetName(), "kubescape-sneeffer") {
							config.SetMyContainerID(getShortContainerID(pod.Status.ContainerStatuses[i].ContainerID))
							continue
						}

						credentialslist := getImageCredentialslist(pod, pod.Status.ContainerStatuses[i].Image)
						sbomObject := sbom.CreateSbomObject(credentialslist, getImageID(pod.Status.ContainerStatuses[i].ImageID))
						vulnObject := vuln.CreateVulnObject(credentialslist, getImageID(pod.Status.ContainerStatuses[i].ImageID), sbomObject)

						containerWatcher.watchedContainers[pod.Status.ContainerStatuses[i].ContainerID] = &watchedContainer{
							containerAggregator:       aggregator.CreateAggregator(getShortContainerID(pod.Status.ContainerStatuses[i].ContainerID), pod.Status.ContainerStatuses[i].State.Running.StartedAt),
							containerAggregatorStatus: true,
							sbomObject:                sbomObject,
							vulnObject:                vulnObject,
							imageID:                   pod.Status.ContainerStatuses[i].ImageID,
							podName:                   pod.Name,
							snifferTimer:              containerWatcher.createTimer(),
							k8sIdentity:               containerWatcher.getK8SIdentityTripplet(pod, pod.Status.ContainerStatuses[i].Image),
							syncChannel: map[string]chan error{
								STEP_GET_SBOM:           make(chan error, 10),
								STEP_GET_UNFILTER_VULNS: make(chan error, 10),
								STEP_AGGREGATOR:         make(chan error, 10),
							},
						}

						logger.Print(logger.INFO, false, "start sniffing on container ID %s of image %s of k8s resource %s\n", pod.Status.ContainerStatuses[i].ContainerID, pod.Status.ContainerStatuses[i].Image, getK8SResourceName(containerWatcher.watchedContainers[pod.Status.ContainerStatuses[i].ContainerID]))

						if config.IsRelaventCVEServiceEnabled() {
							containerWatcher.StartFindRelaventCVEsPrerequisites(pod.Status.ContainerStatuses[i].ContainerID)
						}
						if config.IsContainerProfilingServiceEnabled() {
							containerWatcher.StartContainerProfilingPrerequisites(pod.Status.ContainerStatuses[i].ContainerID)
						}
						go containerWatcher.watchedContainers[pod.Status.ContainerStatuses[i].ContainerID].containerAggregator.StartAggregate(containerWatcher.watchedContainers[pod.Status.ContainerStatuses[i].ContainerID].syncChannel[STEP_AGGREGATOR])
						go containerWatcher.startTimer(pod.Status.ContainerStatuses[i].ContainerID)
					}
				}
				// case watch.Deleted:
				// 	for i := range pod.Status.ContainerStatuses {
				// 		if pod.Status.ContainerStatuses[i].State.Terminated != nil && containerWatcher.isContainerWatched(pod.Status.ContainerStatuses[i].State.Terminated.ContainerID) {
				// 			//before stop watching create relavent sbom
				// 			if containerWatcher.watchedContainers[pod.Status.ContainerStatuses[i].ContainerID] != nil {
				// 				containerWatcher.watchedContainers[pod.Status.ContainerStatuses[i].ContainerID].snifferTimer.Stop()
				// 				containerWatcher.afterTimerActions(pod.Status.ContainerStatuses[i].ContainerID, getK8SResourceName(containerWatcher.watchedContainers[pod.Status.ContainerStatuses[i].ContainerID]))
				// 			}
				// 		}
				// 	}
			}
		}
	}
}
