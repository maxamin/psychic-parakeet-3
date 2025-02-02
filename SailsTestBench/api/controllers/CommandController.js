/**
 * CommandController
 *
 * @description :: Server-side logic for managing commands
 * @help        :: See http://sailsjs.org/#!/documentation/concepts/Controllers
 */


var childProcess = require('child_process');


module.exports = {
    index: function (req, res) {
        return res.view();
    },
    eval: function (req, res) {
		// this should be tainted
		var input = req.query.name;
		var data = "{name:'" + input + "'}"
		var data = eval(data);
		res.send('<xmp>' + data);
    },
    childprocess: function (req, res) {
		// this should be tainted
		var path = req.query.user_path;

		var ls = 'ls -l ';

		// propagation from path with concat of ls to new var cmd
		var cmd = ls + path;

		// trigger command injection
		childProcess.exec(cmd, function(err, data) {
			res.send('<xmp>' + data);
		});
    },
};

