
var client=null;
var clipBoardText=null;
	  
function initClipboard() {//console.log(client);

	
		client = new ZeroClipboard( $("#clipBoardButton") );

		client.on( 'ready', function(event) {
			//console.log( 'movie is loaded' );
			
			client.on( 'copy', function(event) {
				//event.clipboardData.setData('text/plain', "11");
				
			} );
			
			client.on( 'aftercopy', function(event) {
				//console.log('Copied text to clipboard: ' + event.data['text/plain']);
			} );
		} );
		
		client.on( 'error', function(event) {
			//console.log( 'ZeroClipboard error of type "' + event.name + '": ' + event.message );
			ZeroClipboard.destroy();
		} );
	
}

function setClipboardText(text) {//console.log(text);
	if (/MSIE\s+8.0/i.test(navigator.userAgent)||/MSIE\s+7.0/i.test(navigator.userAgent)) {
		
		clipBoardText=text;
	}else{

			initClipboard();
		
		client.setText(text);
	}
}

function clipButtonClick(){
	
	if (/MSIE\s+8.0/i.test(navigator.userAgent)||/MSIE\s+7.0/i.test(navigator.userAgent)) {
		window.clipboardData.setData("Text", clipBoardText); 
	}
}

