# Java_Graphic_Editor

*************************************** Description ***************************************

WARNING /!\ You need to install javafx to run this project properly.

This project allows a client to connect to a server executing graphic editor I created : robi.

Here's how it works :
  
  1- The client enters commands in the client window and sends them to the server to execute them.
  2- The server then processes the commands and returns :
  	 - A message containing confirmation/error and information about the execution (coded in json)
  	 - A screenshot of the graphic editor window is saved and displayed on the client interface.
  3- The client can close the graphic editor by typing quit.

************************************** Instructions **************************************

The "robi" folder contains the server side, the graphic panel the client will edit.
To launch it, you need to run Exercice6.java once you're done with the setup.

The "robic" folder contains the client's interface that you can launch by running "Control.java"

Keep in my mind that you always need to launch your server before your client to establish a connection.

Once everything is running : you can enter the commands below in the client interface to test the graphic editor.

The commands can be used like this :
  ( *object_name* *instruction* *instruction_parameter(s)* )

**************************************** Commands ****************************************

( space addScript addImage (( self filename )(self add im ( Image new filename ) ) ) )
( space addImage alien.gif )
( space add robi (Rect new ) )
( space.robi addScript addRect (( self name w c )( self add name ( Rect new ) )( self.name setColor c )( self.name setDim w w ) ) )
( space.robi addRect mySquare 30 yellow )

(space add robi (Rect new))
(space.robi translate 130 50)
(space.robi setColor yellow)
(space add momo (Oval new))
(space.momo setColor red)
(space.momo translate 80 80)
(space add pif (Image new alien.gif))
(space.pif translate 100 0)
(space add hello (Label new "Hello world"))
(space.hello translate 10 10)
(space.hello setColor black)
