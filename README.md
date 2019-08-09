# android-liveliness-detection
An android application which detects if the faces belong to alive humans or fake posters , using Support Vector Machines with LBP features.


The SVM model is trained via NUAA's client/imposter dataset and you need to create a libsvm folder in your external storage of your phone
and add the model there. 

This project uses OpenCV 2.4.13
The face detection is implemented via Google-Vision

![Real Face](https://user-images.githubusercontent.com/38816094/62768405-71017c80-ba9f-11e9-99c7-50e5e7e2fdd8.jpg)
![Imposter Face](https://user-images.githubusercontent.com/38816094/62768409-73fc6d00-ba9f-11e9-9472-e0397407a628.jpg)



