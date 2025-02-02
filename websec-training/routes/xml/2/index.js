var libxmljs = require('libxmljs')
    fs = require('fs');

module.exports = function (router, level) {
  router.all('/'+level+'*', function (req,res,next){
    res.locals.level = level;
    next();
  });

  router.get('/'+level, function(req, res, next) {
    res.render('xml/'+level+'/index');
  });

  router.post('/'+level+'', function(req, res, next) {
    fs.readFile(process.cwd() + '/routes/xml/'+level+'/db.xml', function (err, file) {
      if (err) return next(err);
      var xmlDoc = libxmljs.parseXml(file.toString());
      var xpath = "/Employees/Employee[UserName/text()=\"" + req.body.username + "\" and "
      + "Password/text()=\"" + req.body.password + "\"]";
      var user = xmlDoc.get(xpath, {noblanks: true});

      if (typeof(user) != "object" || typeof(user.text) == "undefined") {
        req.flash("error", "User not found");
        return res.redirect('back');
      }
      else {
        var loggedInUser = {
          username: user.get("UserName").text(),
          firstName: user.get('FirstName').text(),
          lastName: user.get('LastName').text(),
          isAdmin: user.get('Type').text()
        };

        return res.render('xml/'+level+'/index', {loggedInUser: loggedInUser});
      }
    });
  });
}
