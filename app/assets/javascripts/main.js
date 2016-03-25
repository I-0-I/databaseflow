/* global requirejs:false */
/* global DatabaseFlow:false */
/* global $:false */
requirejs.config({
  baseUrl: '/assets/javascripts',
  paths: {
    lib: '/assets/lib'
  }
});

requirejs([], function() {
  'use strict';

  if(typeof DatabaseFlow !== 'undefined') {
    window.dbf = new DatabaseFlow();
  }
  
  $(document).ready(function() {
    $('.button-collapse').sideNav();
    $('select').material_select();

    var search = $('input#search');
    search.focus(function() { $(this).parent().addClass('focused'); });
    search.blur(function() {
      if (!$(this).val()) {
        $(this).parent().removeClass('focused');
      }
    });
  });
});
