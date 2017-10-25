'use strict'
var fs = require('fs');

var saveImage = function(image, cpf) {
  var dir = '/images/'+cpf+'/';
  var currentTime = new Date().getTime();

  if (!fs.existsSync(dir)){
    console.log('Não existe');
    fs.mkdirSync( __dirname + dir);
  }

  var base64Data = image.replace(/^data:image\/jpeg;base64,/, "");
  var filename = cpf+"-"+currentTime+".jpg";
  
  require("fs").writeFile(__dirname + dir + filename, base64Data, 'base64', function(err) {
    console.log(err);
  });
};

module.exports = saveImage;
