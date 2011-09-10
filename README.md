# Android flow layout

## Introduction

Extended linear layout that wrap its content when there is no place in the current line.

## Demonstration

Demonstration not available yet.

https://github.com/ApmeM/android-flowlayout/raw/master/img/horizontal_album.png
https://github.com/ApmeM/android-flowlayout/raw/master/img/horizontal_album.png
https://github.com/ApmeM/android-flowlayout/raw/master/img/horizontal_portrait.png
https://github.com/ApmeM/android-flowlayout/raw/master/img/no_debug.png
https://github.com/ApmeM/android-flowlayout/raw/master/img/vertical_album.png
https://github.com/ApmeM/android-flowlayout/raw/master/img/vertical_portrait.png


## Installation and usage

Add this module into your solution.

Add the following xml code into your layout/something.xml:

	<org.apmem.tools.layouts.FlowLayout
		xmlns:android="http://schemas.android.com/apk/res/android"
		android:layout_width="fill_parent"
		android:layout_height="wrap_content"
	>
	</org.apmem.tools.layouts.FlowLayout>

To change default horizontal and vertical spacing between elements in layout use the following code:
        
	xmlns:f="http://schemas.android.com/apk/res/your.namespace"
	f:horizontalSpacing="6dip"
	f:verticalSpacing="12dip"

To change default direction use the following code

	f:orientation="vertical"

To override default spacing use the following LayoutParameter in the child View element:

	f:layout_horizontalSpacing="32dip"
	f:layout_verticalSpacing="32dip"

Also if you need to break line before some object even if there is enough space for it in the previous line - use the following LayoutParameter in the child view element:

	f:layout_newLine="true"

## Detailed parameters

Layout parameters:

	* horizontalSpacing - default horizontal spacing between elements

	* verticalSpacing - default vertical spacing between elements

	* debugDraw - draw debug information

	* orientation - line direction. Use one of the following values:

		* horizontal - line will be in horizontal direction, linebreak will create new line

		* vertical - line will be in vertical direction, linebreak will create new column

Child layout parameters:

	* layout_horizontalSpacing - override default horizontal spacing

	* layout_verticalSpacing - override default vertical spacing

	* layout_newLine - brake line before current element even if there is enough place in the current line.

## Copyrights

Created by Artem Votincev (apmem.org)

Distributed under BSD license

