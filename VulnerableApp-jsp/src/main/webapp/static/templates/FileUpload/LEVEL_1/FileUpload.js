function addingEventListenerToUploadImage() {
  document.getElementById("upload").addEventListener("click", function () {
    var form = document.getElementById("file");
    var file = form.files[0];
    var formData = new FormData();
    formData.append("file", file);
    let url =
      "VulnerableApp-jsp/FileUpload/" +
      getCurrentVulnerabilityLevel();
    doPostAjaxCall(uploadImage, url, true, formData);
  });
}
addingEventListenerToUploadImage();

function uploadImage(data) {
  document.getElementById("uploaded_file_info").innerHTML = "File uploaded at location:" + data.fileNameWithPath;
}
