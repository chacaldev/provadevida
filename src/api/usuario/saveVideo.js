'use strict'
var fs = require('fs');

var saveVideo = function(video, cpf) {
  var dir = '/videos\/'+cpf+'/';
  var currentTime = new Date().getTime();

  if (!fs.existsSync(__dirname + dir)){
    console.log('Não existe');
    fs.mkdirSync( __dirname + dir);
  }

  var base64Data = video.replace(/^data:video\/mp4;base64,/, "");
  var filename = cpf+"-"+currentTime+".mp4";
  
  require("fs").writeFile(__dirname + dir + filename, base64Data, 'base64', function(err) {
    console.log(err);
  });
};

module.exports = saveVideo;