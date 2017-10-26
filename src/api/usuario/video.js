'use strict'
var axios = require('axios');
var saveVideo = require('./saveVideo');

var video = function (req, res) {
  var currentTime = new Date().getTime();
  
  var vid = req.body.video;
  var cpf = req.params.cpf;
  console.log(cpf);
  console.log(video);
  saveVideo(vid, cpf);
  res.send({
    validation_status: 0
  });
};

module.exports = video;
