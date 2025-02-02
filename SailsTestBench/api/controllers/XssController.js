/**
 * XssController
 *
 * @description :: Server-side logic for managing xsses
 * @help        :: See http://sailsjs.org/#!/documentation/concepts/Controllers
 */

module.exports = {
    index: function (req, res) {
    
        return res.view();
    },
    
    post: function (req, res) {
		res.set('X-XSS-Protection', '0'); // disable browser xss protection for chrome
		// taint user input 
		var input = req.body.input;
		
		var output = '<html>input: ' + input + '</html>';
			// this should trigger XSS 
		res.send(output);
    },
    
    reflected: function (req, res) {
		res.set('X-XSS-Protection', '0'); // disable browser xss protection for chrome
		// taint user input 
		var input = req.query.input;
		
		var output = '<html>input: ' + input + '</html>';
			// this should trigger XSS 
		res.send(output);
    },
    
    safe: function (req, res) {
		res.set('X-XSS-Protection', '0'); // disable browser xss protection for chrome
		// taint user input 
		var input = escape(req.query.input);
		var output = '<html>input: ' + input + '</html>';
			// this should trigger XSS 
		res.send(output);
    }
    
    
};

