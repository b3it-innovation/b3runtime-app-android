# b3Runtime

***b3Runtime*** is an app inspired by b3's and swedish wellness and games traditions. In a way it is a digitalised 
implementation of **orientering** game, adding a **biathlon** angle to it, where the user shoots answers to questions 
within their category - area of work, education or challenge.

### How it works
//todo add photos
When the app is started, (//this needs to be changed when sing in/login is added) the *game* starts immediately. 
It shows a map and a marker of the first challenge location. Once the user is within a range of the pin a notification 
is triggered and a question within user's category pops up. After user chooses and confirms their answer a response 
popup informs user of their result. Depending on the result an additional route (if answer is wrong) or next challenge 
location (if answer is correct) are suggested and game continues.

#### Authentication
//to be added

#### Permissions
***b3Runtime*** requires *Location permissions* and it is not possible to use the app without them. 
Every time the app is started it checks for permissions and requires them again if they have been revoked 
meanwhile. It makes sure user is informed as well as assisted into granting location permissions.

#### Network
At the moment the app requires the user to have internet connection, at least on start up in order to 
update locations and questions. Then one can play offline.

## Code
***b3Runtime*** is written in Java 8, with an exception of two classes written in Kotlin. 

### Project Structure
To achieve a standardised implementation of methods and handling errors in all *Views* and *ViewModels*,
they extend from abstract base classes like *BaseActivity*, *BaseFragment* or similar, all found in 
the **base package**. The same goes for *ViewModels* that extend *BaseViewModel*.

**data package** contains all data models, local and remote, and all classes that help transfer, convert or manage data. 
Subpackages are divided by topic/feature. 

**di package** contains the Modules.kt* that contains all the logic for *dependency injection*
*please note that this is a Kotlin class

**geofence package** contains *GeofenceManager interface* and *GeofenceManagerImpl* class implementing geofence adding
 and removing logic, as well as *GeofenceBroadcastReciver* thet handles incoming intents, and *GeofencetransitionsJobIntentService*
 that handles the *Jobs* related to geofences

**ui package** contains the Views and ViewModels of all features that are shown to the user. Subpackages 
are divided by topic/feature.

**utils package** contains helper classes used throughout features and packages.

### Architecture
***b3Runtime*** is built in *Model-View-ViewModel architectural pattern* in order to achieve modular and easily testable 
codebase that can scale in any chosen direction. MVVM is a pattern where:

**View** —  is a framework component (usually an activity or a fragment) that enables users to interact with the app.
1. The view is *simple*, containing only Android framework related logic (ex. permissions, intents, local broadcasts) 
and the user interface
2. The view is passive, it only forwards events to the presenter (ex. clicks, lifecycle events) or updates the user 
interface as instructed by the presenter
3. *ViewModels*  are provided using dependency injection
 
**ViewModel** — manages and stores data in a lifecycle aware way
1. One *ViewModel* can hold data for one or more *Views*
2. *ViewModels* only hold data and not business logic. Business logic is contained in *Managers* and *Interactors*
3. Takes care of *View's lifecycle*

**Model** — is all data classes for the ui, persistence or database models

#### Architectural components
**Live Data** a lifecycle aware and observable data holder class, which, as its name suggests, informs about 
changes in data in real time

### Libraries
#### Persistence
**Room** ensures constant and timely flow of data by providing *LiveData* every time data is changed in the remote storage

#### Dependency Injection
For supporting the *clean MVVM pattern* and keeping the code decoupled, we use a *dependency injection library Koin*. 
With it *Interactors, Managers* and other logic containing classes that our *ViewModel* is dependent on, are injected
as singleton instances instead of the *VM* holding references to them. In the same way the *ViewModel* is injected in the *View*.
Other places where *koin* plays a role is in implementing *interfaces* like *Repositories* and *Interactors*,
and by that injecting the implementation class wherever needed. This is secured by the binding in *Modules.kt class*.

### Firebase
At the moment *Firebase* is used in our project as a database. 
In future it will be used for signing in, authentication and possibly other services.
// Please update this readme when this happens.

### GoogleMaps
*GoogleMaps* is used to implement and show the user an interactive map, marking the area of the game. It requires 
installing GooglePlay services SDK, and an API key to access GoogleMaps services.

##### Version History
***b3Runtime*** for Android