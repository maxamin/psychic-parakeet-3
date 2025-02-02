Vagrant.configure("2") do |config|
  config.vm.provision :puppet do |puppet|
    puppet.manifests_path = "manifests"
    puppet.manifest_file = "default.pp"
    puppet.module_path = "modules"
  end

  config.vm.box = "puphpet/debian75-x64"
    # forward apache served ports
  config.vm.network :forwarded_port, guest: 80, host: 8888
    # forward tomcat served ports
  config.vm.network :forwarded_port, guest: 8080, host: 8999

  config.vm.provision "shell", path: "installscripts.sh"

end
