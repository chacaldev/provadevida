'use strict'
var axios = require('axios');
var saveVideo = require('./saveVideo');
var validateVideo = require('./util/validateVideo');

var video = function (req, res) {
  var currentTime = new Date().getTime();
  
  var vid = req.body.video;
  var cpf = req.params.cpf;
  console.log(cpf);
  console.log(vid);
  validateVideo(vid, cpf, function(promise){
    promise.then(function(resp) {
      console.log(JSON.stringify(resp.data));
      saveVideo(vid, cpf);
      res.send({
        validation_status: 0
      });
    })
    .catch(function(err) {
      console.log(err);
      res.send(err);
    });
  });
    
};

module.exports = video;
