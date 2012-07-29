MOCA for Android
================

MOCA for Android is an application to keep [MOCA][] attendees up-to-date with
events, infos and yells from their fine hosts.

The application will be available (eventually) on the Google Play Store.

**FIXME: screenshot(s) here**


Requirements
------------

Rebuilding the application from scratch requires a working [Android SDK][sdk]
installation for API Level 16 (Android 4.1), [Maven][maven] 3.0 or later and
[android-maven-plugin][].

You also going to need to perform some one-time initialization of your build
environment, see `BUILDING.md` for more information.


Contributing
------------

As usual, fork, create a topic branch, hack away and send a pull request when
you're happy with your work :-)

Be aware that setting up Eclipse can be painful due to problems with the
handling of apklibs, instructions will be provided in the wiki.


Wait a minute: where's version 1?!?
-----------------------------------

It was so ugly (and buggy) that I rewrote the entire UI code from scratch and
erased the old one from history.

Believe me, it was really *that* embarassing.


Developed by
------------

* Matteo Panella <morpheus@level28.org>

Large portions of code addressing various Android UI quirks have been lifted
en-masse from [GitHub for Android][gh]. Kudos to them for open-sourcing their
app and shame on Android for making backward-compatible code so hard to write
;-)


Licenses
--------

### Overall application ###

    Copyright (C) 2012 Matteo Panella <morpheus@level28.org>

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

### Banners for sponsors and partners ###

All banners for sponsors and parters are reproduced with explicit permission
from their owners. If you intend to fork this project to use it for other
venues, I kindly ask you to remove those images before committing anything
else.

### Icons and other artwork ###

The MOCA 2012 logo has been designed by [Lopoc][lopoc].

Launcher icons have been generated using the [Android Asset
Studio][assetstudio] and thus they are licensed under a [Creative Commons
Attribution 3.0 Unported License][CC-BY-3.0].


[MOCA]: http://moca.olografix.org/
[sdk]: http://developer.android.com/sdk/index.html
[maven]: http://maven.apache.org/
[android-maven-plugin]: http://code.google.com/p/maven-android-plugin/
[gh]: https://github.com/github/android/
[lopoc]: https://twitter.com/lopoc_
[assetstudio]: http://android-ui-utils.googlecode.com/hg/asset-studio/dist/index.html
[CC-BY-3.0]: http://creativecommons.org/licenses/by/3.0/
