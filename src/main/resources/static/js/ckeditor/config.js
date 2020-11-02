/*
Copyright (c) 2003-2009, CKSource - Frederico Knabben. All rights reserved.
For licensing, see LICENSE.html or http://ckeditor.com/license
*/

CKEDITOR.editorConfig = function( config )
{
	// Define changes to default configuration here. For example:
	// config.language = 'fr';
	// config.uiColor = '#AADC6E';
	config.skin = 'kama';
	config.language = 'zh-cn';
	//config.extraPlugins = "DCampus_UploadPhotos";
	//CKEDITOR.plugins.addExternal( 'DCampus_UploadPhotos', 'plugins/DCampus_UploadPhotos/' ,'plugins.js');
	/**'UploadPhotos'**/
	config.toolbar = 'Basic';
	config.toolbar_Basic = [
			['Bold','Italic','Underline','Strike'],['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],['Link','Unlink','Image','Table'],'/',['Format','Font','FontSize','TextColor','BGColor'],['Maximize']
			];
	config.toolbarCanCollapse = true;
	config.entities = true;
	config.resize_enabled = false;
	config.uiColor = '#EAF2FA';
	config.dialog_backgroundCoverColor = 'transparent';
};
