# 📝 System Overview —> Architecture

## 1. Initial System Structure

### Different microservices
The application is split into five microservices, as depicted in the UML Component diagram below. The main task of each microservice is explained afterwards.

![UML Component diagram](/markdown-files/assets/ComponentDiagram.png "UML Component diagram")
  
**Frontend (React)**: Handles interaction between User and the Backend

**SafeRouteService (SpringBoot)**: Can be split into two smaller services. The DangerPointService handles the CRUD operations of danger points and makes them available to the RouteService, which calculates a safe route (probably through an external API). 

**MessageService (SpringBoot)**: Contacts the emergency contacts, if the AIService or the User detects danger.

**UserService (SpringBoot)**: Safes user information, like preferences when talking to the AI, emergency contacts, ..., which it exposes to the MessageService for emergency contacts, or the AI Service for preferences when talking to it.

**AIService (LangChain)**: Main task is to talk to the user, to accompany them. Optionally a SpeachService handles text to speach and speach to text, to make "phone calls" possible.

*Userdata, Danger Points and Chats are saved in a **MongoDB** database.*

### Interaction between User and Microservices
How the user interacts with this app to invoke the different microservices can be viewed in the use case diagram below and explained afterwards.

![UML Use Case diagram](/markdown-files/assets/UseCaseDiagram.svg "UML Use Case diagram")

1. If the viewer plans a route to get home safely the SafeRouteService queries unsafe locations, displays them and plans a safe route
2. When the user chats with the AI, the Frontend interacts with the AI Service. During the chat, the AI monitors the words and trys to detect an emergency. If it detects one, it calls the MessageService.
3. In case the user feels unsafe, he can tag the location as unsafe. This calls the SafeRouteService, to safe the location to the database.
4. The user does not only need to rely on the AI to sense danger, but can also trigger an emergency themselfes. This directly calls the MessageService, which then internally contacts the emergency contacts.

### Class diagram

A rough estimation of how the explained microservices would look as classes, can be viewed in the picture below.

![UML Class diagram](/markdown-files/assets/UMLClassDiagram.png "UML Class diagram")


## 2. First Product Backlog
Can be viewed in [this document](/markdown-files/ProductBacklog.md).