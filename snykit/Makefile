NAME := snykit
NAMESPACE := garethr
CONFIG := snykit.yaml
IMAGE := $(NAMESPACE)/$(NAME)

RENDER = ytt -f $(CONFIG) -f values.yaml
BUILD = kbld -f -
DEPLOY = kapp deploy -a $(NAME) -f - -y

DBUILD = docker build
PUSH = docker push

check-buildkit:
ifndef DOCKER_BUILDKIT
	$(error You must enable Buildkit for Docker, by setting DOCKER_BUILDKIT=1)
endif

build: check-buildkit
	@$(RENDER) | $(BUILD)

deploy: check-buildkit
	@$(RENDER) | $(BUILD) | $(DEPLOY)

render: check-buildkit
	@$(RENDER)

push: check-buildkit
	@$(DBUILD) -t $(IMAGE):latest .
	@$(DBUILD) --build-arg BASE=ruby:2.7.0-slim -t $(IMAGE):slim .
	@$(PUSH) $(IMAGE):latest
	@$(PUSH) $(IMAGE):slim
