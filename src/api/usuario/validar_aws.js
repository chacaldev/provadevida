'use strict'
var fs = require('fs');
var AWS = require('aws-sdk');
AWS.config.update({region: 'us-east-1'});

var imagem = function (req, res) {
  var rekognition = new AWS.Rekognition();

  fs.readFile('/Users/admin/Downloads/kairos-elizabeth2.jpg', 'base64', function(err, data){
    if (err) {
      throw err;
    }
    // create a new base64 buffer out of the string passed to us by fs.readFile()
    const buffer = new Buffer(data, 'base64');

    var params = {
      CollectionId: "myphotos", 
      FaceMatchThreshold: 95, 
      Image: {
        Bytes: buffer
      }, 
      MaxFaces: 5
    };

    rekognition.searchFacesByImage(params, function(err, data) {
      if (err) console.log(err, err.stack); // an error occurred
      else     console.log(data);           // successful response
      res.send(data);
    });
  }); 
};

module.exports = imagem;