# Android flow layout

## Introduction

Extended linear layout that wrap its content when there is no place in the current line.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apmem.tools/layouts/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.apmem.tools/layouts/)

## Demonstration

Orientation: HORIZONTAL, Gravity: FILL, LayoutDirection: LTR

![](https://github.com/ApmeM/android-flowlayout/raw/master/img/LANDSCAPE_LTR_FILL_HORIZONTAL_DEBUG.png)
![](https://github.com/ApmeM/android-flowlayout/raw/master/img/PORTRAIT_LTR_FILL_HORIZONTAL_DEBUG.png)

Orientation: HORIZONTAL, Gravity: RIGHT & BOTTOM, LayoutDirection: RTL

![](https://github.com/ApmeM/android-flowlayout/raw/master/img/LANDSCAPE_RTL_RIGHTBOTTOM_HORIZONTAL_DEBUG.png)
![](https://github.com/ApmeM/android-flowlayout/raw/master/img/PORTRAIT_RTL_RIGHTBOTTOM_HORIZONTAL_DEBUG.png)

Orientation: VERTICAL, Gravity: CENTER, LayoutDirection: LTR

![](https://github.com/ApmeM/android-flowlayout/raw/master/img/LANDSCAPE_LTR_CENTER_VERTICAL_DEBUG.png)
![](https://github.com/ApmeM/android-flowlayout/raw/master/img/PORTRAIT_LTR_CENTER_VERTICAL_DEBUG.png)

Debug is switched off:

![](https://github.com/ApmeM/android-flowlayout/raw/master/img/LANDSCAPE_LTR_FILL_HORIZONTAL_NODEBUG.png)
![](https://github.com/ApmeM/android-flowlayout/raw/master/img/PORTRAIT_LTR_FILL_HORIZONTAL_NODEBUG.png)

## Installation and usage

Take from maven repository (<http://search.maven.org/#search%7Cga%7C1%7Corg.apmem.tools>, <http://mvnrepository.com/search.html?query=org.apmem.tools>) or add FlowLayout and other components to your solution

Add it as dependency in Gradle as:

	compile 'org.apmem.tools:layouts:1.8@aar'

Or maven

        <dependency>
            <groupId>org.apmem.tools</groupId>
            <artifactId>layouts</artifactId>
            <version>1.8</version>
            <scope>provided</scope>
        </dependency>

Add the following xml code into your layout/something.xml:

	<org.apmem.tools.layouts.FlowLayout
		xmlns:android="http://schemas.android.com/apk/res/android"
		android:layout_width="fill_parent"
		android:layout_height="wrap_content"
	>
	</org.apmem.tools.layouts.FlowLayout>

To change default direction use the following code

	android:orientation="vertical"

To change layout direction use the following code

	xmlns:f="http://schemas.android.com/apk/res/your.namespace"
	f:layoutDirection="rtl"
	
Android gravity now supported (in combination with elements weight):

        f:weightDefault="1.0"
        android:gravity="fill"

To override default spacing between elements use default android margins in the child View element:

	android:layout_marginTop="32dip"
	android:layout_marginRight="32dip"

Also if you need to break line before some object even if there is enough space for it in the previous line - use the following LayoutParameter in the child view element:

	f:layout_newLine="true"

## Detailed parameters

Layout parameters:

	* android:orientation - line direction. Use one of the following values:

		* horizontal - line will be in horizontal direction, linebreak will create new line

		* vertical - line will be in vertical direction, linebreak will create new column

        * android:gravity - standart android gravity supported

	* debugDraw - draw debug information

        * weightDefault - default weight value for child elements. Used to fill line in case of Gravity.FILL_HORIZONTAL | Gravity.FILL_VERTICAL

        * layoutDirection - direction of inner child elements:

                *  ltr - left to right direction

                *  rtl - right to left direction

Child layout parameters:

	* android:layout_margin* - override default spacings

	* android:layout_gravity - standart aandroid gravity supported

        * layout_weight - weight of the element. If not specified "layout.defaultWight" is used.

	* layout_newLine - brake line before current element even if there is enough place in the current line.

## Copyrights

   Copyright 2011, Artem Votincev (apmem.org)
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
