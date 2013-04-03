
tradable-start-np
=================
A starting point for creating tradable apps. Just clone and start hacking away. This starting point  includes code that:

* Places a market order and uses the client order id.
* Sends a pending order an watch for its execution
* Monitors the change in position once an order is placed
* Makes use of tracks and places orders on different tracks.

If you've read the API documentation on our website and want to start coding straight away using this project as a base for your app, here's what you need to do:

Cloning and opening the project in Eclipse
-------------------------------------------------------

1. Make sure you are running Eclipse Juno or a later version with the Maven and WindowBuilder plugins installed. If this is not the case, fear not. You can install them quite easily form within Eclipse. For Maven: 
  * Go to Help -> Install New Software...
  * If you don't have the juno realeases site set up as a repository. Click Add, name it however you wish, then in the location space write: http://download.eclipse.org/releases/juno. (Of course if you have a later version of Eclipse, the link will be adapted to the name of that version)
  * Now in the search bar, search for m2e. Once the results pop up, select the **m2e - Maven Integration for Eclipse** packages from the *General Purpose Tool* and *Collaboration* packages. Also select the **Xtend M2E extensions**. Then next -> next -> finish

 For WindowBuilder The procedure is similar:
  * Go to Help -> Install New Software...
  * Click Add, name the repository "WindoWBuilder" or the like. You will find the link to include [here](http://www.eclipse.org/windowbuilder/download.php). For instance, for Juno you might copy: http://download.eclipse.org/windowbuilder/WB/release/R201209281200/4.2/
  * Select all the packages, then click next -> next -> finish   
  

2. Now That you are all setup and that you have made sure that you have setup your eclipse platform to develop tradable apps as per [this](http://link.to.be.provided) link. Let's import the project to Eclipse.
 * Open up your Shell (or git Shell) then navigate to your Eclipse Workspace using:   

            $ cd path/to/your/worksapce
 * Then clone the git repository using:

            $ git clone https://github.com/john-dwuarin/tradable-start-np.git [name_of_firectory_to_copy_to]
 * Once the directory is cloned in your Worskpace, you will probably want to remove the origin remote from your local repo, as you probably will not want to be linked to this repository as no actual work is done on it:

            $ git remote rm origin
 * Open up Eclipse and import the project by doing File -> Import -> Maven -> Existing Maven Projects. Then Browse to the directory you just imported from github, select /pom.xml and then press Finish.

 That's it. The project should now be open in Eclipse with the name tradable-start-np.

3. You can now go about renaming the project and the classes by right clicking on them and refractor -> rename. You should also update the JRE System Library the project uses by right-clicking on JRE System Library and using the latest version available. You will have to edit the *src/main/resources/META-INF/MANIFEST.MF* and the *src/main/resources/META-INF/spring/app-context.xml* files as per the link mentioned in point 2. to reflect your project's and/or your company 's name.**But most importantly, you will have to edit the *pom.xml* file (preferably using the xml view of Eclipse) and change the GroupId and ArtifactId values.** Remember that the GroupId dvalue should be an inverse .com notation and should reflect the name of the entity you are doing the project for and ArtifactId is quite simply the name of your project.   


Open up the Howto page to find out how this page was written.