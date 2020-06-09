# QRGenerator
* An app to generate QR Code from json Object containing user's name, email, dob, address and face image as base64 string.

* Standard QR Codes can hold up to 3Kb of data. So, Face Image should be compressed as much as possible with good quality. ( Grayscale image with less than 2 kb of size is working fine here)

## App Workflow :
* Enter basic details of the user: 1.Name, 2.Email, 3.Date Of Birth, 4.Address

* Select compressed image or Grayscale image that can be fit into QR Code. 

* image will be converted into base64 string format and JSON Object will be generated with following keys:
name, email, dob, address, image

Json Object structure:
  
  {
  
    "name" : "abc",
    "email" : "abc@xyz.com",
    "dob" : "1-1-2000",
    "address" : "Bangalore, India",
    "image": " /9j/4AAQSkZJRgABAQEAYADXQ25Ny6Z3k......................(base64string)"
  }

* QR Code will be generated and displayed in imageView. Also, it can be shared via other applications using "ShareQR" button.
