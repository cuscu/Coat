$(function () { // on dom ready

    var cy = cytoscape({
        container: document.getElementById('cy'),
        elements: cy3json.elements,
        style: cytoscape.stylesheet()
            .selector('node')
            .css({
                'content': 'data(id)',
                'text-valign': 'center',
                'color': 'white',
                'text-outline-width': 2,
                'text-outline-color': '#888'
            })
            .selector('edge')
            .css({
                'target-arrow-shape': 'triangle',
                'width': 4,
                'line-color': '#ddd',
                'target-arrow-color': '#ddd'
            })
            .selector('.highlighted')
            .css({
                'background-color': '#61bffc',
                'line-color': '#61bffc',
                'target-arrow-color': '#61bffc',
                'transition-property': 'background-color, line-color, target-arrow-color',
                'transition-duration': '0.5s'
            }),
        layout: {
            name: 'breadthfirst',
            circle: false,
            spacingFactor: 1.75,
            boundingBox: undefined,
            avoidOverlap: true, // prevents node overlap, may overflow boundingBox if not enough space
            roots: undefined, // the roots of the trees
            //name: 'circle',
            //sort: function (a, b) {
            //    return a.data('score') - b.data('score')
            //}
            //name: 'concentric',
            //fit: true,
            //minNodeSpacing: 10, // min spacing between outside of nodes (used for radius adjustment)
            //concentric: function () {
            //    return -this.data('score');
            //},
            //levelWidth: function (nodes) {
            //    return 0.5;
            //},
            animate: true, // whether to transition the node positions
            animationDuration: 500, // duration of animation in ms if enabled
        }
    });
}); // on dom ready