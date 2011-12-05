var pca_viz_pcX = 0;
var pca_viz_pcY = 1;

var flot_point_radius = 4;
var flot_shadow_size = 8;


var no_of_significant_topics_per_document=5;
// Which topics should these be? The ones that are the most salient for the document,
// i.e. the ones for which the probability distribution is the highest for the document

var no_of_significant_words_for_topic=5;

var pca_viz_colors = [
  "#7FFF00", "#FF6347", "#7FFFD4", "#DDA0DD",
  "#B0C4DE", "#FFE4C4", "#B22222"
];

function pca_viz_plot_map() {
  var series = [];
  for (var i = 0; i < pca_viz.items.length; i++) {
    var start = pca_viz.breaks[i];
    var end = pca_viz.breaks[i + 1];
    var data = new Array(end - start);
    for (var j = start; j < end; j++) {
      data[j - start] = [
        pca_viz.data[j][pca_viz_pcX],
        pca_viz.data[j][pca_viz_pcY]
      ];
    }

    series.push({
      "label": pca_viz.items[i].name, 
      "lines": { "show": false },
      "points": {"radius": flot_point_radius, "show": true },
      "shadowSize": flot_shadow_size,
      "color": pca_viz_colors[i],
      "data": data
    });
  }

  var options = {//jQuery.extend(pca_viz.view, {
    "grid": { "clickable": true, "hoverable": true },
    "xaxis": { "zoomRange": [0.1, 2], "panRange": [-1, 1] },
    "yaxis": { "zoomRange": [0.1, 2], "panRange": [-1, 1] },
    "zoom": { "interactive": true },
    "pan": { "interactive": true }
  };

  jQuery.plot(jQuery("#pca-viz-map"), series, options);
}

jQuery("#pca-viz-map").addClass("flot_lww");
jQuery("#pca-viz-map").show();

jQuery(document).ready(function() { pca_viz_plot_map();; });


var pca_viz_variance = {
  //"label": "Variance for component",
  "lines": { "show": true },
  "points": { "radius": flot_point_radius, "show": true },
  "shadowSize": flot_shadow_size,
  /*"color": "#D0CDAC",*/
  "data": pca_viz.variance
};

function pca_viz_plot_variance() {
  var pcX_variance = {
    "label": "PC for first dimension (x axis)",
    "lines": { "show": false },
    "points": { "radius": flot_point_radius, "show": true },
    "shadowSize": flot_shadow_size,
    "color": "#990055",
    "data": [ pca_viz.variance[pca_viz_pcX] ]
  };

  var pcY_variance = {
    "label": "PC for second dimension (y axis)",
    "lines": { "show": false },
    "points": { "radius": flot_point_radius, "show": true },
    "shadowSize": flot_shadow_size,
    "color": "#99DDFF",
    "data": [ pca_viz.variance[pca_viz_pcY] ]
  };


  var options = { "grid": {"clickable": true, "hoverable": true } };

  jQuery.plot(jQuery("#pca-viz-variance"), [pca_viz_variance, pcX_variance, pcY_variance], options);
}

jQuery("#pca-viz-variance").addClass("flot_lww");
jQuery("#pca-viz-variance").show();

jQuery(document).ready(function() { pca_viz_plot_variance();; });

jQuery("#pca-viz-variance").bind("plotclick", function (event, pos, item) {
  if (item) {
    if (item.seriesIndex == 0) {
      pca_viz_pcY = pca_viz_pcX;
      pca_viz_pcX = item.dataIndex;
    } else {
      var tmp = pca_viz_pcX;
      pca_viz_pcX = pca_viz_pcY;
      pca_viz_pcY = tmp;
    }
    pca_viz_plot_map();
    pca_viz_plot_variance();
    pca_viz_plot_loadings();
  }
});


/***** Pie chart stuff *******/

function pie(data) {
  /*
	// For now, choose the number of significant topics per document to be TWO (hardcoded). This needs to be parametrized later.
	// var no_of_significant_topics_per_document=2;

	var pie_data = new Array(no_of_significant_topics_per_document+1);
	var pietopic = new Array(no_of_significant_topics_per_document);
	var most_significant_topics_for_this_document  = [];
	var probabilities_of_the_most_significant_topics_for_this_document = [];

	// pca_viz.topics contains ALL the topics, in a specific order.
	// The distributions of all those topics are in the array 
	// data.document.features, in the same order of topics as the 
	// order of topics in pca_viz.topics

	// We want to pick the topics with the highest distributions. 
	// So we will iterate through all the topics in pca_viz.topics, 
	// picking the top FOUR, and simultaneously keeping track of the indices
	// of those FOUR. (Recall that FOUR is the number of topics, hard-coded 
	// for now, which will be parametrized later.
	
	// Once those FOUR topics with the highest values have been found
	// from data.document.features, we will read off what those FOUR
	// topics were, from looking up the topics corresponding to those
	// FOUR indices from pca_viz.topics. These FOUR topics will then form 
	// the labels of the pie chart, while the actual distributions corresponding
	// to these FOUR topics will form FOUR of the FIVE segments of the pie
	// chart (the additional segment having the catch-all label of "Other",
	// and having a content of (1- (sum of the probabilities of the other FOUR
	// contents)).
	
	// Obtain the number of topics from pca_viz.topics -- this number is
	// equal to the number of elements in the pca_viz.topics array.
	// Maybe this will need to be parametrized later.
	
	var no_of_topics = pca_viz.topics.length;
	
	var no_of_words_constituting_each_topic = (pca_viz.topics[0]).length;
	
	// For the moment, find the most significant topics for the document 
	// by a brute-force-search. We will refine this to a more clever 
	// method later.
	
	var index_of_topic_with_currently_highest_probability = 0;
	var index_of_topic_with_currently_second_highest_probability = 0;
	var index_of_topic_with_currently_third_highest_probability = 0;
	var index_of_topic_with_currently_fourth_highest_probability = 0;
	
	var probability_of_topic_with_currently_highest_probability=0;
	var probability_of_topic_with_currently_second_highest_probability=0;
	var probability_of_topic_with_currently_third_highest_probability=0;
	var probability_of_topic_with_currently_fourth_highest_probability=0;    
		   	   
	// while sorting the entire data.document.features array would have been conceptually simpler,
	// it would have been less inefficient than necessary; all we really care about here is to find
	// the top m topics -- we don't need to sort the entire array; all we are doing below is to 
	// store, sorted, just the top m topics at any given instant, as we iterate through the data.document.features 
	// array
	
	var temp, tempindex;        
	for (var i = 0; i < no_of_topics ; i++) {
			if (data.document.features[i] > probability_of_topic_with_currently_fourth_highest_probability) {
				index_of_topic_with_currently_fourth_highest_probability = i;
				probability_of_topic_with_currently_fourth_highest_probability = data.document.features[i];
			}
			if (probability_of_topic_with_currently_fourth_highest_probability >
				 probability_of_topic_with_currently_third_highest_probability) {
				 
				 // interchange the topics with currently the fourth and third highest probabilities
				 
				 temp = probability_of_topic_with_currently_fourth_highest_probability;
				 probability_of_topic_with_currently_fourth_highest_probability=probability_of_topic_with_currently_third_highest_probability;
				 probability_of_topic_with_currently_third_highest_probability=temp;
				 
				 tempindex = index_of_topic_with_currently_fourth_highest_probability;
				 index_of_topic_with_currently_fourth_highest_probability=index_of_topic_with_currently_third_highest_probability;
				 index_of_topic_with_currently_third_highest_probability=tempindex;
				 
			} 
			if (probability_of_topic_with_currently_third_highest_probability >
				 probability_of_topic_with_currently_second_highest_probability) {
				 
				 // interchange the topics with currently the third and second highest probabilities
				 
				 temp = probability_of_topic_with_currently_third_highest_probability;
				 probability_of_topic_with_currently_third_highest_probability=probability_of_topic_with_currently_second_highest_probability;
				 probability_of_topic_with_currently_second_highest_probability=temp;
				 
				 tempindex = index_of_topic_with_currently_third_highest_probability;
				 index_of_topic_with_currently_third_highest_probability=index_of_topic_with_currently_second_highest_probability;
				 index_of_topic_with_currently_second_highest_probability=tempindex;
			} 
			if (probability_of_topic_with_currently_second_highest_probability >
				 probability_of_topic_with_currently_highest_probability) {
				 
				 // interchange the topics with currently the second highest and highest probabilities
				 
				 temp = probability_of_topic_with_currently_second_highest_probability;
				 probability_of_topic_with_currently_second_highest_probability=probability_of_topic_with_currently_highest_probability;
				 probability_of_topic_with_currently_highest_probability=temp;
				 
				 tempindex = index_of_topic_with_currently_second_highest_probability;
				 index_of_topic_with_currently_second_highest_probability=index_of_topic_with_currently_highest_probability;
				 index_of_topic_with_currently_highest_probability=tempindex;
			} 
	}        
	
	most_significant_topics_for_this_document.push(data.document.features[index_of_topic_with_currently_highest_probability]);
	most_significant_topics_for_this_document.push(data.document.features[index_of_topic_with_currently_second_highest_probability]);
	most_significant_topics_for_this_document.push(data.document.features[index_of_topic_with_currently_third_highest_probability]);
	most_significant_topics_for_this_document.push(data.document.features[index_of_topic_with_currently_fourth_highest_probability]);

    probabilities_of_the_most_significant_topics_for_this_document.push(probability_of_topic_with_currently_highest_probability);
	probabilities_of_the_most_significant_topics_for_this_document.push(probability_of_topic_with_currently_second_highest_probability);
	probabilities_of_the_most_significant_topics_for_this_document.push(probability_of_topic_with_currently_third_highest_probability);
	probabilities_of_the_most_significant_topics_for_this_document.push(probability_of_topic_with_currently_fourth_highest_probability);
	
	for( var i = 0; i<no_of_significant_topics_per_document; i++){
			
			pietopic[i] = [];
			for (var j = 0; j < no_of_words_constituting_each_topic; j++) {
			    
				  //pietopic[i].push(most_significant_topics_for_this_document[j].slice(0,5).join(" ") );
				  pietopic[i].push(pca_viz.topics[most_significant_topics_for_this_document[j]].slice(0,2).join(" ") );
			 }		  
			 pie_data[i] = { "label": pietopic[i], "data": probabilities_of_the_most_significant_topics_for_this_document[i] };
	}
	
	var sum_of_the_most_significant_probabilities_for_this_document = 0;
	for( var i = 0; i<no_of_significant_topics_per_document; i++){
			sum_of_the_most_significant_probabilities_for_this_document += probabilities_of_the_most_significant_topics_for_this_document[i];
	}		
	pie_data[i+1] = { "label": "Other", "data": (1-sum_of_the_most_significant_probabilities_for_this_document) };
  */

  
  var indexed = [];
  for (var i = 0; i < data.document.features.length; i++) {
    indexed.push([i, data.document.features[i]]);
  }

  indexed.sort(function(a, b) {
    return a[1] < b[1];
  });

  var prob_mass_seen = 0.0;
  var pie_data = [];
  for (var i = 0; i < no_of_significant_topics_per_document; i++) {
    prob_mass_seen += indexed[i][1];

    var topic = pca_viz.topics[indexed[i][0]].slice(0, no_of_significant_words_for_topic);
    var words = jQuery.map(topic, function(e, i) { return e[0]; });

    pie_data.push({ 
      //"label": pca_viz.topics[indexed[i][0]].slice(0, no_of_significant_words_for_topic).join(" "),
      "label": words.join(" "),
      "data": indexed[i][1]
    });
  }
  pie_data.push({ "label": "Other", "data": 1 - prob_mass_seen });

	var pca_viz_piechart = {
	  "series": { 
		  "pie": {
			"show": true
		  }
	  },  
	};
	
	
	jQuery("#pca-viz-piechart").addClass("flot_lww");
	jQuery("#pca-viz-piechart").show();
	
	
	jQuery.plot(jQuery("#pca-viz-piechart"), pie_data, pca_viz_piechart);
	
	// jQuery(document).ready(function() { pca_viz_plot_piechart(pie_data);; });

} // end of function "pie"



function pca_viz_plot_loadings() {
  var dataX = pca_viz.loadings[pca_viz_pcX];
  var dataY = pca_viz.loadings[pca_viz_pcY];

  var data = new Array(dataX.length);
  for (var i = 0; i < data.length; i++) {
    data[i] = {
      "index": i,
      "coords": [ dataX[i], dataY[i] ],
      "magnitude": dataX[i] * dataX[i] + dataY[i] * dataY[i]
    };
  }

  data.sort(function(a, b) {
    return (b.magnitude - a.magnitude);
  });
 
  var series = [];
  for (var i = 0; i <  no_of_significant_topics_per_document; i++) {
    var topic = pca_viz.topics[data[i].index].slice(0, no_of_significant_words_for_topic); 
    var words = jQuery.map(topic, function(e, i) { return e[0]; });

    series.push({
      //"label": pca_viz.topics[data[i].index].slice(0, no_of_significant_words_for_topic).join(" "),
      "label": words.join(" "),
      "lines": { "show": true },
      "points": { "radius": flot_point_radius, "show": true },
      "shadowSize": flot_shadow_size,
      //"color": "#990055",
      "data": [ [ 0.0, 0.0 ], data[i].coords ]
    });
  }
  series.push({
    "points": { "radius": flot_point_radius, "show": true },
    "color": "#000000",
    "data": [ [ 0.0, 0.0 ] ]
  });

  var options = {
    "xaxis": { "min": -1.0, "max": 1.0 },
    "yaxis": { "min": -1.0, "max": 1.0 }
  }; // { "grid": {"clickable": true, "hoverable": true } };

  jQuery.plot(jQuery("#pca-viz-loadings"), series, options);
}

jQuery("#pca-viz-loadings").addClass("flot_lww");
jQuery("#pca-viz-loadings").show();

jQuery(document).ready(function() { pca_viz_plot_loadings();; });

var pca_load_document = function(event, pos, item) {
  
  if (item) {
    jQuery.getJSON('api/text/' + pca_viz.items[item.seriesIndex].id + '/' + item.dataIndex + '.json', function(data) {
      jQuery('#drilldown-title').text(data.text.title + ' (' + data.document.seq + ')');
      jQuery('#drilldown-author').text(data.text.author + ' (' + data.text.year + ')');
      jQuery('#drilldown-link').text("Visit source collection");
      jQuery('#drilldown-link').attr("href", data.document.url);
      
     
      // jQuery(jQuery('#drilldown-text').html(data.document.html)).Tokenizer();
       
      // Replace every occurrence of a word called "word" (say) in the text document,
      // by <span class="to_highlight">word</span>, if "word" is part of the words 
      // characterizing the topic that is currently under the mouse.
            
      var indexed = [];
         
      // for (var i = 0; i < data.document.features.length; i++) {
      
      for (var i = 0; i <  no_of_significant_topics_per_document; i++) {    
          indexed.push([i, data.document.features[i]]);
      }
      indexed.sort(function(a, b) {
           return a[1] < b[1];
      });
      	
      // Now replace each word in the document that matches any of the most significant words 
      // belonging to any of the most significant topics.  The replacement here is really a
      // "spanification" -- the original word is replaced by the same word sitting within a 
      // span element that takes care of the highlighting. 
      // We're doing the replacement using regular expressions.)
      
      // "spanify" the word for later highlighting
      // by replacing the word "word" in the following manner, with:
      // <span class="to_highlight">word</span>
            
      // Do this by (conceptually) concatenating (A), (B) and (C) together, where
      // (A) is all the characters in the string before the jth
      // position, (B) the new replacement, and (C) all the characters in the string
      // from the (j+t)th position, where t is the number of letters in the word that is being 
      // replaced. We won't deal with this at the level of individual characters however;
      // instead, we will simply use the "replace" method for string objects.

      for (var i = 0;  i <  no_of_significant_topics_per_document; i++) {      
            for (var j = 0;  j <  pca_viz.topics.length; j++) {
               
                word_to_be_replaced = pca_viz.topics[indexed[i][0]].slice(j, j+1);
                var re = "/" + word_to_be_replaced + "/gip";
               
                // This, however potentially messes up case/capitalization in the 
                // document text. How to deal with this?
                var replacement = "<span style=\"background-color:#FFFF00\""
                                             + word_to_be_replaced
                                                 + "</span>";
                
                data.document.html = data.document.html.replace(re,replacement); 
             }   
       } 
       /* for (var j = 0; j < data.document.html.length; j++) {
          // looking at the word that begins at the jth position in the document 
      
        	  var all_matches_false = true;
      	  
      	  // 
      	  // We're doing pattern-matching with regexps to check for words in data.document.html
          // that match any of the words in the topic currently under consideration
          // (note: we're looking only at the first few specified words in the topic.
          
      	  for (var i = 0; i < no_of_significant_topics_per_document; i++) {
      	      var word_matches = true;
      	     
      	     
      	     // the i-th word in the topic is:
             // pca_viz.topics[indexed[i][0]].slice(i, i+1)
             
      	     for (var k = 0; ((k < (pca_viz.topics[indexed[i][0]].slice(i,i+1)).length) && (data.document.html.charAt(j+k) != " ")     
      	          && (data.document.html.charAt(j+k) != ";")
                       && (data.document.html.charAt(j+k) != ",")
                          && (data.document.html.charAt(j+k) != ":")
                             && (data.document.html.charAt(j+k) != "-")); k++) {
                             
             // It was done this way above, instead of simply implementing with regexps because, for all we 
             // know, topics in some weird situations could well contain "words" that have punctuation
             // marks inside them, while the definition of a "word", as far as the text being 
             // displayed is concerned, should be determined by word separators. Here, we have listed
             // some word separators -- ideally, to be separated out and maintained as a list somewhere
             // in one place so as not to clutter up the code.  -- SB
             
      	          	    
                 if (data.document.html.charAt(j+k) != ((pca_viz.topics[indexed[i][0]].slice(i, i+1)).toString()).charAt(k))
                      word_matches = false;
              
             }
          }   
          if (word_matches == true) {
            all_matches_false = false; 
          }
          
          //determine length of the word which began at the jth position in the string
          var word_length =0;
          while ( (data.document.html.charAt(j+word_length) != " ") && (data.document.html.charAt(j+word_length) != ".")
                     && (data.document.html.charAt(j+word_length) != ";")
                       && (data.document.html.charAt(j+word_length) != ",")
                          && (data.document.html.charAt(j+word_length) != ":")
                             && (data.document.html.charAt(j+word_length) != "-")) { 
            word_length++;
          }  
         
          if (all_matches_false == false) {
            // i.e. at least one word in the topic has matched the word in the 
            // document which began at the jth position in the document
          
            // then "spanify" the word for later highlighting
            // by replacing the word "word" in the following manner, with:
            // <span class="to_highlight">word</span>
            
            // Do this by concatenating (A), (B) and (C) together, where
            // (A) all the characters in the string before the jth
            // position, (B) the new replacement, and (C) all the characters in the string
            // from the (j+t)th position, where t is the number of letters in the word that is being 
            // replaced

            
            data.document.html = data.document.html.substr(0,j) 
                                   + "<span style=\"background-color:#FFFF00\"" 
                                       + data.document.html.substr(j,word_length)
                                         + "</span>" 
                                            + data.document.html.substr(j+word_length);
            
          }
       }    */
       
      jQuery('#drilldown-text').html(data.document.html)
         
      jQuery('#pca-viz-piechart').addClass("flot_lww");
      jQuery('#pca-viz-piechart').show();
      pie(data);
    });
  }
};

var pca_bind_hover = function() {
  jQuery("#pca-viz-map").bind("plothover", function (event, pos, item) {
    if (item) {
      pca_load_document(event, pos, item);
      jQuery('#pca-viz-map').unbind('plothover');
      setTimeout(pca_bind_both, 250);
    }
  });
};

var pca_bind_click = function() {
  jQuery("#pca-viz-map").bind("plotclick", function (event, pos, item) {
    if (item) {
      pca_load_document(event, pos, item);
      jQuery('#pca-viz-map').unbind('plothover');
      setTimeout(pca_bind_both, 3000);
    }
  });
};

var pca_bind_both = function() {
  pca_bind_hover();
  pca_bind_click();
};

pca_bind_both();

/* Parser/Tokenizer */

// This is the file "Tokenizer 1.0.1 Source", downloaded
// from http://flesler.blogspot.com/2008/03/string-tokenizer-for-javascript.html
// on June 20, 2011

/**
 * Tokenizer/jQuery.Tokenizer
 * Copyright (c) 2007-2008 Ariel Flesler - aflesler(at)gmail(dot)com | http://flesler.blogspot.com
 * Dual licensed under MIT and GPL.
 * Date: 2/29/2008
 *
 * @projectDescription JS Class to generate tokens from strings.
 * http://flesler.blogspot.com/2008/03/string-tokenizer-for-javascript.html
 *
 * @author Ariel Flesler
 * @version 1.0.1
 */

/* 
;(function(){
	
	var Tokenizer = function( tokenizers, doBuild ){
		if( !(this instanceof Tokenizer ) )
			return new Tokenizer( tokenizers, onEnd, onFound );
			
		this.tokenizers = tokenizers.splice ? tokenizers : [tokenizers];
		if( doBuild )
			this.doBuild = doBuild;
	};
	
	Tokenizer.prototype = {
		parse:function( src ){
			this.src = src;
			this.ended = false;
			this.tokens = [ ];
			do this.next(); while( !this.ended );
			return this.tokens;
		},
		build:function( src, real ){
			if( src )
				this.tokens.push(
					!this.doBuild ? src :
					this.doBuild(src,real,this.tkn)
				);	
		},
		next:function(){
			var self = this,
				plain;
				
			self.findMin();
			plain = self.src.slice(0, self.min);
			
			self.build( plain, false );
				
			self.src = self.src.slice(self.min).replace(self.tkn,function( all ){
				self.build(all, true);
				return '';
			});
			
			if( !self.src )
				self.ended = true;
		},
		findMin:function(){
			var self = this, i=0, tkn, idx;
			self.min = -1;
			self.tkn = '';
			
			while(( tkn = self.tokenizers[i++]) !== undefined ){
				idx = self.src[tkn.test?'search':'indexOf'](tkn);
				if( idx != -1 && (self.min == -1 || idx < self.min )){
					self.tkn = tkn;
					self.min = idx;
				}
			}
			if( self.min == -1 )
				self.min = self.src.length;
		}
	};
	
	if( window.jQuery ){
		jQuery.tokenizer = Tokenizer;//export as jquery plugin
		Tokenizer.fn = Tokenizer.prototype;
	}else
		window.Tokenizer = Tokenizer;//export as standalone class
		
})();

var rows = [ ], row = rows[0] = [ ]; 
// var tokenizerOutput = new Tokenizer( [',',';'],
var tokenizerOutput = new Tokenizer( [' '],
  function( text, isSeparator ){
     if( isSeparator ){
         if( text == ';' ){//new row
             row = [ ];
             rows.push(row);
         }
     }else{   
         row.push(text);
     } 
  }
);

*/

/* The highlight plugin 

// This is the file jquery.highlight-3.js, downloaded
// from http://johannburkard.de/blog/programming/javascript/highlight-javascript-text-higlighting-jquery-plugin.html
// on June 20, 2011

highlight v3

Highlights arbitrary terms.

<http://johannburkard.de/blog/programming/javascript/highlight-javascript-text-higlighting-jquery-plugin.html>

MIT license.

Johann Burkard
<http://johannburkard.de>
<mailto:jb@eaio.com>

*/

jQuery.fn.highlight = function(pat) {
 function innerHighlight(node, pat) {
  var skip = 0;
  if (node.nodeType == 3) {
   var pos = node.data.toUpperCase().indexOf(pat);
   if (pos >= 0) {
    var spannode = document.createElement('span');
    spannode.className = 'highlight';
    var middlebit = node.splitText(pos);
    var endbit = middlebit.splitText(pat.length);
    var middleclone = middlebit.cloneNode(true);
    spannode.appendChild(middleclone);
    middlebit.parentNode.replaceChild(spannode, middlebit);
    skip = 1;
   }
  }
  else if (node.nodeType == 1 && node.childNodes && !/(script|style)/i.test(node.tagName)) {
   for (var i = 0; i < node.childNodes.length; ++i) {
    i += innerHighlight(node.childNodes[i], pat);
   }
  }
  return skip;
 }
 return this.each(function() {
  innerHighlight(this, pat.toUpperCase());
 });
}; 

jQuery.fn.removeHighlight = function() {
 return this.find("span.highlight").each(function() {
  this.parentNode.firstChild.nodeName;
  with (this.parentNode) {
   replaceChild(this.firstChild, this);
   normalize();
  }
 }).end();
}; 




