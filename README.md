Server components of the cids system [![Build Status](http://ci.cismet.de/buildStatus/icon?job=cids-server)](https://ci.cismet.de/job/cids-server/)
===

#cids README
![Figure 0](http://www.cismet.de/images/faq-images/fis_wasser1.png)

Figure 0: The cids Navigator

The cids product suite consists of a set of services, applications, software components, management tools, development tools, and application programming interfaces (APIs) for the management, integration, and development of heterogeneous information systems with a special focus on interactive geo-spatial systems. It provides a distributed integration platform, which is particularly useful for workflows that need a combination of information and processes from different source systems such as GIS systems, relational databases, and simulation models. In this way it already provides and supports a considerable number of functionalities required for complex geospatial information systems, including user management and access control, search and discovery of relevant information and advanced interactive 2D visualisation (OGC WMS and WFS clients). 

![Figure 1](http://www.cismet.de/images/cidswhitepaper/fig1.png)

Figure 1: cids Client-Server Architecture

Figure 1: cids Client-Server Architecture shows that the SMS Framework is based on a client-server architecture in which an arbitrary number of client instances and server components co-exist in a service network, thus ensuring scalability and reliability. The components shown in Figure 9 are explained in detail in the following sections.
The main building blocks of the framework are the Navigator (client), the Kernel, and a set of system management tools. The building blocks and the components are shown in Figure 2: Framework Building Blocks.

![Figure 2](http://www.cismet.de/images/faq-images/cids_components.png)

Figure 2: Framework Building Blocks

The Kernel represents a network of distributed services and consists of the following four components:

* Integration Base	
The Integration Base is a distributed meta database which consists of a generic meta data model placed in a relational DBMS (Data Base Management System). Figure 3: Generic Meta Data Model of the Integration Base shows an excerpt of generic meta data model. It is the basis for a concrete information system and is able to describe arbitrary objects (real-world objects, services, models, geographical features, other information systems, etc.), their attributes (e.g. geographical location) and relationships by means of so-called meta classes and objects. The generic meta data model also supports a dynamic navigational catalogue structure, users, groups and permissions, and many other properties.

![Figure 3](http://www.cismet.de/images/cidswhitepaper/fig3.png)

* Domain Server		
The Domain Server is the interface to an Integration Base and is responsible for the translation of the generic meta data structure into concrete meta objects and classes, thereby also supporting the creation and updating of meta data. It is also responsible for the construction of the dynamic catalogue structure at runtime.
* Registry Server	
The Registry Server is responsible for the resolution of distributed user privileges needed for the enforcement of access rights, the resolution of the distributed catalogue structure needed for the navigation, and the coordination of the distributed search. It also provides service infrastructure related functionalities like server name resolution, network monitoring, status information, etc.
* Broker	
The Broker is the interface to the clients and hides the distribution aspects of the system. It acts like a proxy and delegates client requests to the appropriate Domain Servers.

The Navigator, shown as a customized version in Figure 0, is the default client for user interactions with the system. It offers a uniform, user-specific view of the integrated information systems and is particularly useful for cross-system search and retrieval in space, time, and textual content. It can also be used to manage the information in the network. It can be use e.g. as the management client for OGC compliant data access and model execution services, which are described as customised information classes in the underlying meta data model, thus offering common functionalities for the various users of the system. The Navigator features a plug-in mechanism that allows to easily integrate custom extensions in order to support workflows for specific end user tasks. The Navigator provides the following core functionalities through dedicated GUI components:

* Catalogue	
The Catalogue presents a tree for navigation of the distributed catalogue and provides basic information about objects (Figure 4: Catalogue Attribute View). It supports caching of the dynamically constructed catalogue structure which can be distributed among several Domain Server and Integration Base instances. Furthermore, it provides functions to manage the catalogue structure as well as meta data objects (Figure 5: Catalogue Node and Object Management).

![Figure 4](http://www.cismet.de/images/cidswhitepaper/fig4.png)

Figure 4: Catalogue Attribute View

![Figure 5](http://www.cismet.de/images/cidswhitepaper/fig5.png)

Figure 5: Catalogue Node and Object Management

* Search	
There is a highly customisable interface to the dynamic search capabilities of the Kernel, which takes a combination of different meta object attributes into account to provide thematic search to end users. It also features sophisticated geospatial search capabilities and direct interaction with the Map Component.

* Renderer	
Renderers are used to provide thematic, context and user dependent views on certain topics. The Navigator is equipped with a set of default Renderers for a variety of different object types and supports the integration of a customised Renderer on a per-object class basis. Figure 6: Custom Rainfall Render as Client for OGC SOS shows a custom Renderer that communicates with a Sensor Observation Service


![Figure 6](http://www.cismet.de/images/cidswhitepaper/fig6.png)

Figure 6: Custom Rainfall Render as Client for OGC SOS

* Editors	
Editors are used to manipulate meta objects in a uniform manner, such as scheduled model runs as shown in Figure 7: Default Attribute Editor. As already mentioned, meta objects can represent arbitrary physical or virtual objects from simple documents over geographical features to complex workflows. In addition to automatically generated editors, which provide attribute based editing functionality, custom editors are also supported. Custom Editors can be used for example, to provide advanced functionalities for the configuration of models or to support very specific tasks.

![Figure 7](http://www.cismet.de/images/cidswhitepaper/fig7.png)


Figure 7: Default Attribute Editor


* Map Component


The Map Component shown in Figure 8: Map Component (cismap) is based on cismap, a highly sophisticated 2D map viewer for geo data services such as Web Map Services (WMS) and Web Feature Services (WFS) which comply with the standards of the Open Geospatial Consortium (OGC). Since cismap supports both powerful visualisation and editing functionalities for geospatial information (including arbitrary geolocated meta objects), it can support geospatial information systems end users during configuration, management and decision and planning tasks. Some of the main features of cismap are 

- asynchronous WMS and WFS requests to load several WMS layers in parallel
- dynamic addition of services, including drag and drop of the server URL
- a powerful and easy to use geometry editor for the manipulation of geo-data
- complete integration with cismap and support for the visualisation and manipulation of meta objects
- a customisable user interface and the ability to save the layout on a per-user basis
- the representation of geographical features on the map as complex widgets
- customisable print and report generation facilities

![Figure 7](http://www.cismet.de/images/cidswhitepaper/fig8.png)

Figure 8: Map Component (cismap)

The cids product suite provides, in addition to the client and server components described in the previous sections, a set of System Management Tools. They support system managers during the installation and maintenance of the SUDPLAN applications as well as modellers during the integration and configuration of mathematical models to be used within SUDPLAN applications. They can also be used to carry out general system administration tasks like user management. The System Management Tools include the following.


* ABF	
ABF, which stands for “Administrators’ Best Friend,” is a powerful front-end for the configuration of the meta data base. It can be used to define new meta classes and their attributes, relationships, dynamic catalogue structures and so on. 

![Figure 7](http://www.cismet.de/images/cidswhitepaper/fig9.png)

Figure 9: Administrators Best Friend (ABF)

* Server Console	
The Server Console is used to monitor and control a server component or a whole service network

![Figure 7](http://www.cismet.de/images/cidswhitepaper/fig10.png)

Figure 10: Server Console

* JPresso	
JPresso is a powerful ETL-Tool (Extraction, Transformation and Load Tool) for the integration of heterogeneous data-sources. It supports visual mappings that describe the connection between data-sources and the cids Integration Base.	

![Figure 7](http://www.cismet.de/images/cidswhitepaper/fig11.png)

Figure 11: JPresso

An application, based on cids components and tools described in this document, already fulfils most of the technical requirements of a generic geospatial infrastructure, open source components, web-based technologies, user-friendly graphical interfaces, security and access control mechanisms, and scalability. It represents a sound basis for the implementation core functionalities of complex geospatial informations systems like resource discovery, sharing and publishing of information and automation of tasks. 


License
=======

cids-server is distributed under [LGPLv3](https://github.com/cismet/cids-server/blob/dev/LICENSE)

:-)

