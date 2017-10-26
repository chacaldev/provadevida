'use strict'
var axios = require('axios');
const cv = require('opencv4nodejs');
const { grabFrames } = require('./utils');
const saveVideo = require('../saveVideo');
var keys = require('../../keys');
var fs = require('fs');

var validateVideo = function(video, cpf, cb) {
  var dir = '/temp/';

  var base64Data = video.replace(/^data:video\/mp4;base64,/, "");
  var filename = cpf+".mp4";
  var fileUri = __dirname + dir + filename;

  console.log(fileUri);
  
  fs.writeFileSync(fileUri, base64Data, 'base64')
   
  const delay = 20;
  console.log(fileUri);
  grabFrames(fileUri, delay, (frame) => {
    const resizedImg = frame.resizeToMax(640);

    cv.imwrite(__dirname+'/temp/'+cpf+'.jpg', resizedImg);
    var bitmap = fs.readFileSync(__dirname+'/temp/'+cpf+'.jpg');
    // convert binary data to base64 encoded string
    var img =  new Buffer(bitmap).toString('base64');
    
    var config = {
      headers: {
        'Content-Type': 'application/json',
        'app_id': keys.app_id,
        'app_key': keys.app_key,
      }
    };
    
    var payload = {
      "image": img,
      "gallery_name":"gal-" + cpf,
    };
    cb(axios.post('https://api.kairos.com/recognize', payload, config));
  
  });

  
    
};

module.exports = validateVideo;