# ProjectRocketC
### What is this
This is a side project of some random student studying in unimelb. The thing is on google play so you can potientially download it.

It's a game about a rocket flying in space collecting stars and avoiding planet.

It's still a work in progress, though it's very close to finish. There isn't much coding left to do, only a few more drawings of rocket and maybe some backgroud musics and better sound track.

Oh yeah this game will need a in game money system too so player can earn game money and buy better rocket. 

### The Code
The game was first written in Java until about half way I ported it to Kotlin. Apparently Kotlin is a better language than Java (it is newer and more convenient).

The App only contains a single activity. The activity is responsible of all the UI of the game (animations, showing and hiding views). It also initialize an instance of ProcessingThread class, and an instance of MySurfaceView which is a subclass of SurfaceView. 

MySurfaceView is responsible for drawing each frame of the game. It does this by holding an instance of Layers class, which holds all the datas needed to draw the frame. For every frame, the datas in Layers is modified by ProcessingThread according to the user input and physics of the game, and passed into OpenGL which draws the frame accordingly.

The sole resposibility of ProcessingThread is, as mentioned above, to modify Layers for the next frame. It does this by creating and malipulating instances of subclasses of Shape class. 

Every instance of Shape is made up of other instances of shape. The only exception is TriangularShape, which malipulates arrays in Layers storing vertexes of triangles and their color.

This is of course a oversimplified version of my code. Many of the relationship mention above are infact indirect. If you really want to know more you can potientionally look into my actually code. It's very readable and well structured so if you can't read it it's definetely your fault not mine.

### Screenshot of The Game
haha too bad for you there isn't any, if you want to know what the game is like download it on Google Play.
