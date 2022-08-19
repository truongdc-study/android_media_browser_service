# MediaBrowserService
Create - create New Class and extents form class MediaBrowserviceCompat()

[1] Initialize the media session

overide onCreate() menthod 
  * Create and initialize the media session
  * Set the media session callback
  * Set the media session token
[2] Manage client connection

A [MediaBrowService] has two menthod that handle client connections : 
- onGetRoot() -> control access to the service 
- onLoadChildren() -> provide the ability for a client to build and display a menu
of the [MediaBrowserService's] content hierarchy.

# Controlling client connections with onGetRoot()
The [onGetRoot] method return the root node og the content hierarchy. 
If method return null, the connection is refused.

To allow clients connect your service and browser its media content, onGetRoot() must return a noll-null
BrowserRoot which is a root  ID that represent your content hierarchy.

To allow client to connect to your mediaSession without browsing , onGetRoot() must still return noll-null
BrowserRoot , but the root ID should represent a empty content hierarchy. 

A typical implementation of [onGetRoot()] might look like this.

override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
): MediaBrowserServiceCompat.BrowserRoot {
    return if (allowBrowsing(clientPackageName, clientUid)) {
        MediaBrowserServiceCompat.BrowserRoot(MY_MEDIA_ROOT_ID, null)
    } else {
        MediaBrowserServiceCompat.BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null)
    }
} 


#Communicating content with onLoadChildren()

After the client connects, it can traverse the content hierarchy by making repeated calls to 
MediaBrowserCompat.subscribe()to build a local representation of the UI

The subscribe() method sends the callback onLoadChildren() to the service, which returns a list 
of MediaBrowser.MediaItem objects.

Each MediaItem has a unique ID string, which is an opaque token
When a client wants to open a submenu or play an item, it passes the ID. Your service is responsible for 
associating the ID with the appropriate menu node or content item.

A simple implementation of onLoadChildren() might look like this:

override fun onLoadChildren(
        parentMediaId: String,
        result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>
){
    if (MY_EMPTY_MEDIA_ROOT_ID == parentMediaId) {
        result.sendResult(null)
        return
    }
    val mediaItems = emptyList<MediaBrowserCompat.MediaItem>()
    if (MY_MEDIA_ROOT_ID == parentMediaId) {
    } else {
        // Examine the passed parentMediaId to see which submenu we're at,
        // and put the children of that menu in the mediaItems list...
    }
    result.sendResult(mediaItems)
}
