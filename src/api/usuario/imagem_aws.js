'use strict'
var fs = require('fs');
var AWS = require('aws-sdk');
AWS.config.update({region: 'us-east-1'});

var imagem = function (req, res) {
  var rekognition = new AWS.Rekognition();

  // var params = {
  //   CollectionId: "myphotos"
  // };
  // rekognition.createCollection(params, function(err, data) {
  //   if (err) console.log(err, err.stack); // an error occurred
  //   else     console.log(data);           // successful response
  //   /*
  //   data = {
  //   CollectionArn: "aws:rekognition:us-west-2:123456789012:collection/myphotos", 
  //   StatusCode: 200
  //   }
  //   */
  // });

  fs.readFile('/Users/admin/Downloads/kairos-elizabeth.jpg', 'base64', function(err, data){
    if (err) {
      throw err;
    }
    // create a new base64 buffer out of the string passed to us by fs.readFile()
    const buffer = new Buffer(data, 'base64');

    var params = {
      CollectionId: "myphotos", 
      DetectionAttributes: [
      ], 
      ExternalImageId: "myphotoid", 
      Image: {
        Bytes: buffer
      }
    };
  
    rekognition.indexFaces(params, function(err, data) {
      if (err) console.log(err, err.stack); // an error occurred
      else     console.log(data);
      res.send(data);
    });
  });
  
  
  
};

module.exports = imagem;
