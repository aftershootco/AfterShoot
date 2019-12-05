We're using Google Cloud AutoML Vision Edge to train Machine Learning models to identify potentially bad pictures and so far we've managed to identify Blurred, Over Exposed, Under Exposed and Blinks in a picture.  
These models are tensorflow_lite models that run directly on device without requiring an internect connection.  

Next steps are to train ML models to identify more potential flaws in an image.

### Blurred  
![](https://i.imgur.com/KBnHicS.png)  

### Exposure  
![](https://i.imgur.com/nP400il.png)  

### Blinks  
![](https://i.imgur.com/u9OPDp0.png)
