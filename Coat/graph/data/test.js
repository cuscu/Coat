var cy3json = {
    elements: {
        "nodes": [
            {"data": {"id": 1, "name": "RELN1"}},
            {"data": {"id": 2, "name": "DISC1"}}
        ],
        "edges": [
            {"data": {"source": 1, "target": 2}}
        ]
    },
    style: [
        {
            selector: 'node',
            style: {
                'content': 'data(id)'
            }
        }
    ]
};
