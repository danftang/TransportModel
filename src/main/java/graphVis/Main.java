package graphVis;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.view.Viewer;

import static java.lang.Thread.sleep;

/**
 * Created by daniel on 15/06/17.
 */
public class Main {
    static Graph graph = new SingleGraph("Test graph");
    static int i = 1000;

    public static void moveAlongEdge(Sprite s) {
        double d;
        for(d=0.0; d<=1.0; d+=0.0125) {
            s.setPosition(d);
            ++i;
            graph.addAttribute("ui.screenshot", "./frame"+i+".png");
            try {
                sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {

        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addEdge("AB","A","B");
        graph.addEdge("BC","B","C");

        SpriteManager sman = new SpriteManager(graph);
        Sprite s = sman.addSprite("S1");

        Viewer viewer = graph.display();
        s.attachToEdge("AB");
        s.setPosition(0.0);
        moveAlongEdge(s);
        s.attachToEdge("BC");
        s.setPosition(0.0);
        moveAlongEdge(s);

    }
}
