# Music downloader for VK.com
1. Navigate to page with audio on vk.com in Chrome (important!)
2. Scroll to see all of them (they are lazy loading)
3. Save page as HTML
4. Open it in app

## Configuration
Download threads could be configured using system property
`ua.atamurius.vk.music.Downloader.threads`

## Build
Default maven profile build JAR without dependencies with debug output to stdout.
To build executable JAR use `production` profile.

`mvn -P production` builds executable JAR and places it to `builds`