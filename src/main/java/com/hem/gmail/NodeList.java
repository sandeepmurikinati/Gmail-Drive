package com.hem.gmail;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data

public class NodeList {
    List<Node> nodes;
    private long currentNumber;

    NodeList() {
        nodes = new ArrayList<>();
    }

    public long nextNumber() {
        return ++currentNumber;
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public Node findNode(String id) {
        return nodes.stream().filter(each -> ("Node" + each.getId()).equalsIgnoreCase(id)).findAny().get();
    }

    public void deleteNode(String substring) {
        Node node = findNode(substring);
        nodes.remove(node);
    }
}
