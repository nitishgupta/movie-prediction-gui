$(function() {

	var array = new Array();
	var existing="hello";
	
	$('#progress').css('width', '0');
	$('#progress_text').html('0% Complete');

	var form = $('#userForm');
	form.submit(function() {
		if($('#age').val()==0 )
		{
			alert("Enter age");
			return false;
		}
		if($('#zip').val() == 0)
		{
			alert("Enter zip");
			return false;
		}
		$.ajax({
			type : form.attr('method'),
			url : form.attr('action'),
			data : form.serialize(),
			dataType : 'json',
			success : function(data) {
				existing = data.existing;
				array[0] = (data.first);
				array[1] = data.second;
				array[2] = data.third;
				array[3] = data.fourth;
				array[4] = data.fifth;
				array[5] = data.sixth;
				array[6] = data.seventh;
				array[7] = data.eighth;
				array[8] = data.ninth;
				array[9] = data.tenth;
			}
		});

		$('#progress_text').html('50% Complete');
		$('#progress').css('width', '170px');

		// slide steps
		$('#first_step').slideUp();
		$('#second_step').slideDown();

		return false;
	});

	var form2 = $('#movieForm');
	var movie = "";
	form2.submit(function() {
		$.ajax({
			type : form2.attr('method'),
			url : form2.attr('action'),
			data : form2.serialize(),
			success : function(data) {
				movie = data;
				
				var fields = new Array(movie, $('#age').val() + ','
						+ $('#sex').val() + ',' + $('#profession').val() + ','
						+ $('#zip').val(), existing, array[0], array[1], array[2],
						array[3], array[4], array[5], array[6], array[7], array[8], array[9]);

				var tr = $('#third_step tr');
				tr.each(function() {
					// alert( fields[$(this).index()] )
					$(this).children('td:nth-child(2)').html(
							fields[$(this).index()]);
				});
			}
		});

		$('#progress_text').html('100% Complete');
		$('#progress').css('width', '339px');

		// slide steps
		$('#second_step').slideUp();
		$('#third_step').slideDown();

		return false;
	});
	
	var form3=$('#ratingForm');
	var rating;
	var view;
	form3.submit(function(){
		$.ajax({
			type: form3.attr('method'),
			url: form3.attr('action'),
			datatype: 'json',
			success: function(data){
				rating=data.rating;
				view=data.view;
				if(view=="YES")
					alert("YES "+rating);
				else
					var x = confirm("NO "+rating);
				if(x==true)
					window.location.reload();
			}
		});
		
		/*$('#third_step').slideDown();
		$('#second_step').slideDown();
		$('#first_step').slideDown();
		*/return false;
	});

});/*
	 * //original field values var field_values = { //id : value 'sex' : 'sex',
	 * 'age' : 'age', 'profession' : 'profession', 'zip' : 'zip', 'lastname' :
	 * 'last name', 'email' : 'email address' };
	 * 
	 * 
	 * 
	 * //inputfocus // $('select#sex').inputfocus({ value: field_values['sex']
	 * }); // $('input#age').inputfocus({ value: field_values['age'] }); //
	 * $('select#profession').inputfocus({ value: field_values['profession'] }); //
	 * $('input#zip').inputfocus({ value: field_values['zip'] }); //
	 * $('input#firstname').inputfocus({ value: field_values['firstname'] }); //
	 * $('input#email').inputfocus({ value: field_values['email'] });
	 * 
	 * 
	 * 
	 * 
	 * //reset progress bar $('#progress').css('width','0');
	 * $('#progress_text').html('0% Complete');
	 * 
	 * //first_step $('form').submit(function(){ return false; });
	 * $('#submit_first').click(function(){ //remove classes //$('#first_step
	 * input').removeClass('error').removeClass('valid');
	 * 
	 * //ckeck if inputs aren't empty //var fields = $('#first_step
	 * input[type=text], #first_step input[type=password]'); var error = 0;
	 * 
	 * var param={sex:'#sex'.val(), age:'#age'.val(),
	 * profession:'#profession'.val(), zip:'#zip'.val()};
	 * alert('#profession'.val()); alert("FUCK"); $.ajax({ type: 'get', url:
	 * 'UserFormhandle', data: param, success: function(data){ var result=data;
	 * alert(result); $.each(result.Items,function(index,item){ alert(item); }); }
	 * });
	 * 
	 * 
	 * fields.each(function(){ var value = $(this).val(); if( value.length<4 ||
	 * value==field_values[$(this).attr('id')] ) { $(this).addClass('error');
	 * $(this).effect("shake", { times:3 }, 50);
	 * 
	 * error++; } else { $(this).addClass('valid'); } });
	 * 
	 * if(!error) { if( $('#password').val() != $('#cpassword').val() ) {
	 * $('#first_step input[type=password]').each(function(){
	 * $(this).removeClass('valid').addClass('error'); $(this).effect("shake", {
	 * times:3 }, 50); });
	 * 
	 * return false; } else { //update progress bar
	 * $('#progress_text').html('33% Complete');
	 * $('#progress').css('width','113px');
	 * 
	 * //slide steps $('#first_step').slideUp(); $('#second_step').slideDown(); } }
	 * else return false; });
	 * 
	 * 
	 * $('#submit_second').click(function(){ //remove classes $('#second_step
	 * input').removeClass('error').removeClass('valid');
	 * 
	 * var emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/; var
	 * fields = $('#second_step input[type=text]'); var error = 0;
	 * fields.each(function(){ var value = $(this).val(); if( value.length<1 ||
	 * value==field_values[$(this).attr('id')] || ( $(this).attr('id')=='email' &&
	 * !emailPattern.test(value) ) ) { $(this).addClass('error');
	 * $(this).effect("shake", { times:3 }, 50);
	 * 
	 * error++; } else { $(this).addClass('valid'); } });
	 * 
	 * if(!error) { //update progress bar $('#progress_text').html('66%
	 * Complete'); $('#progress').css('width','226px');
	 * 
	 * //slide steps $('#second_step').slideUp(); $('#third_step').slideDown(); }
	 * else return false;
	 * 
	 * });
	 * 
	 * 
	 * $('#submit_third').click(function(){ //update progress bar
	 * $('#progress_text').html('100% Complete');
	 * $('#progress').css('width','339px');
	 * 
	 * //prepare the fourth step var fields = new Array( $('#username').val(),
	 * $('#password').val(), $('#email').val(), $('#firstname').val() + ' ' +
	 * $('#lastname').val(), $('#age').val(), $('#gender').val(),
	 * $('#country').val() ); var tr = $('#fourth_step tr'); tr.each(function(){
	 * //alert( fields[$(this).index()] )
	 * $(this).children('td:nth-child(2)').html(fields[$(this).index()]); });
	 * 
	 * //slide steps $('#third_step').slideUp(); $('#fourth_step').slideDown();
	 * });
	 * 
	 * 
	 * $('#submit_fourth').click(function(){ //send information to server
	 * alert('Data sent'); });
	 * 
	 * });
	 */