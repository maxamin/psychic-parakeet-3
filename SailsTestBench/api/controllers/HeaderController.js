/**
 * HeaderController
 *
 * @description :: Server-side logic for managing headers
 * @help        :: See http://sailsjs.org/#!/documentation/concepts/Controllers
 */

module.exports = {
    index: function (req, res) {
        return res.view();
    },
    redir: function (req, res) {
		var path = req.query.user_path;
		res.redirect(path);
    },
    
};

