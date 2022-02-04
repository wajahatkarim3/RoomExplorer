<div align="left"><p><a href="https://android.jlelse.eu/easyflipviewpager-the-flip-animations-for-your-viewpager-fd66b34f4703"><img alt="Detail article about RoomExplorer" align="center" src="https://img.shields.io/badge/%F0%9F%93%84%20-NEW-red.svg" /></a>&nbsp;
The article on why and how this library was created is now published. You can <a href="https://android.jlelse.eu/exploring-android-room-database-with-room-explorer-4d760fdea9de">read it on this link here. →</a>.
</p></div>

# 🔎 RoomExplorer
A quick and easy database viewer and manager library for your Room databases. 

![](https://androidweekly.net/issues/issue-406/badge) [![Download](https://api.bintray.com/packages/wajahatkarim3/RoomExplorer/com.wajahatkarim3.RoomExplorer/images/download.svg) ](https://bintray.com/wajahatkarim3/RoomExplorer/com.wajahatkarim3.RoomExplorer/_latestVersion) [![API](https://img.shields.io/badge/API-15%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=15) [![Say Thanks!](https://img.shields.io/badge/Say%20Thanks-!-1EAEDB.svg)](https://saythanks.io/to/wajahatkarim3) [![](https://img.shields.io/badge/$-donate-ff69b4.svg?maxAge=2592000&amp;style=flat)](https://www.paypal.me/WajahatKarim/5)

<div>
  <sub>Built with ❤︎ by
  <a href="https://twitter.com/WajahatKarim">Wajahat Karim</a> and
  <a href="https://github.com/wajahatkarim3/RoomExplorer/graphs/contributors">
    contributors
  </a>
</div>
<br/>

<div>
  <img src="https://github.com/wajahatkarim3/RoomExplorer/blob/master/Art/RoomExplorer_Demo.gif" width="280px" />
</div>

✔️ Changelog
=========
Changes exist in the [releases](https://github.com/wajahatkarim3/RoomExplorer_Demo/releases) tab.

# 🎯 Features

* View all your tables data in tabular format
* Insert rows to your tables
* Update rows
* Delete rows
* Delete tables
* Drop tables
* Write your own custom queries and view the results. (Create statements, joins, etc)
* Change data in the tables and see how you application responds


💻 Installation
============
Add this in your app's `build.gradle` file:
```groovy
dependencies {
  implementation 'com.wajahatkarim:roomexplorer:1.0.1'
}
```

Or add RoomExplorer as a new dependency inside your pom.xml

```xml
<dependency> 
  <groupId>com.wajahatkarim</groupId>
  <artifactId>roomexplorer</artifactId> 
  <version>1.0.1</version>
  <type>pom</type> 
</dependency>
```

# ❔ How to Use

To view your Room databases in Room Explorer, just call this line and pass your database class and database name in the `show()` method.

```kotlin
   RoomExplorer.show(context, MyRoomDB::class.java, "MyRoomDBName")
```

👨 Developed By
============
```
Wajahat Karim
```
- Website (http://wajahatkarim.com)
- Twitter (http://twitter.com/wajahatkarim)
- Medium (http://www.medium.com/@wajahatkarim3)
- LinkedIn (http://www.linkedin.com/in/wajahatkarim)

# 👍 How to Contribute
1. Fork it
2. Create your feature branch (git checkout -b my-new-feature)
3. Commit your changes (git commit -am 'Add some feature')
4. Push to the branch (git push origin my-new-feature)
5. Create new Pull Request

## 📃 License

```
Copyright 2019 Wajahat Karim

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
