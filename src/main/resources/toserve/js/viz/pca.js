var pca_viz_pcX = 0;
var pca_viz_pcY = 1;

var flot_point_radius = 4;
var flot_shadow_size = 8;

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
    //"zoom": { "interactive": true },
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


// var series = Math.floor(Math.random()*10)+1;
// for( var i = 0; i<series; i++){
//		pie_data[i] = { "label": "Series"+(i+1), "data": Math.floor(Math.random()*100)+1 };
//}

// For now, choose the number of significant topics per document to be FOUR (hardcoded). This needs to be parametrized later.
var no_of_significant_topics_per_document=4;

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

/* for( var i = 0; i<no_of_topics; i++){
        for (var j = 0; j < 5; j++) {
   		      pietopic.push(pca_viz.topics[j].slice(0,5).join(" ") );
         }		  
         //pietopic.push({
             // eventually, this should be replaced by the actual features (sorted)
             // for now, it's randomly generated (for testing).
    	     // "data": Math.floor(Math.random()*100)+1
         //});
		 pie_data[i] = { "label": pietopic[i], "data": Math.floor(Math.random()*100)+1 };
} */


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

var temp;        
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
        } 
        if (probability_of_topic_with_currently_third_highest_probability >
             probability_of_topic_with_currently_second_highest_probability) {
             
             // interchange the topics with currently the third and second highest probabilities
             
             temp = probability_of_topic_with_currently_third_highest_probability;
             probability_of_topic_with_currently_third_highest_probability=probability_of_topic_with_currently_second_highest_probability;
             probability_of_topic_with_currently_second_highest_probability=temp;
        } 
        if (probability_of_topic_with_currently_second_highest_probability >
             probability_of_topic_with_currently_highest_probability) {
             
             // interchange the topics with currently the second highest and highest probabilities
             
             temp = probability_of_topic_with_currently_second_highest_probability;
             probability_of_topic_with_currently_second_highest_probability=probability_of_topic_with_currently_highest_probability;
             probability_of_topic_with_currently_highest_probability=temp;
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
   		      pietopic[i].push(most_significant_topics_for_this_document[j].slice(0,5).join(" ") );
         }		  
		 pie_data[i] = { "label": pietopic[i], "data": probabilities_of_the_most_significant_topics_for_this_document[i] };
}

var sum_of_the_most_significant_probabilities_for_this_document = 0;
for( var i = 0; i<no_of_significant_topics_per_document; i++){
		sum_of_the_most_significant_probabilities_for_this_document += probabilities_of_the_most_significant_topics_for_this_document[i];
}		
pie_data[i+1] = { "label": "Other", "data": (1-sum_of_the_most_significant_probabilities_for_this_document) };


var pca_viz_piechart = {
 //"label": "",
  "series": { 
  	  "pie": {
        "show": true
  	  }
  },  
 //"data": data
};

function pca_viz_plot_piechart() {
    
    jQuery.plot(jQuery("#pca-viz-piechart"), pie_data, pca_viz_piechart);
}


jQuery("#pca-viz-piechart").addClass("flot_lww");
jQuery("#pca-viz-piechart").show();

jQuery(document).ready(function() { pca_viz_plot_piechart();; });



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
  for (var i = 0; i < 5; i++) {
    series.push({
      "label": pca_viz.topics[data[i].index].slice(0, 5).join(" "),
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
      jQuery('#drilldown-text').html(data.document.html);
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

