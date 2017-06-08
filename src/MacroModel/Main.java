package MacroModel;

public class Main {

    public static void main(String[] args) {
        int i;
        TransportGraph TfL = new TransportGraph();

        // ----------- Add nodes
        for(i = 0; i<4; ++i) {
            TfL.nodes.add(new Node());
        }
        Node tangTown   = TfL.nodes.get(0);
        Node danHeights = TfL.nodes.get(1);
        Node jonsBottom = TfL.nodes.get(2);
        Node culleyCorner = TfL.nodes.get(3);

        // ----------- Add edges
        TfL.edges.add(new Edge(culleyCorner, tangTown));
        TfL.edges.add(new Edge(tangTown, jonsBottom));
        TfL.edges.add(new Edge(jonsBottom, danHeights));
        TfL.edges.add(new Edge(danHeights, culleyCorner));
        TfL.edges.add(new Edge(danHeights, tangTown));
        TfL.edges.add(new Edge(tangTown, danHeights));

        // ----------- Add vehicles
        for(i = 0; i<2; ++i) {
            TfL.vehicles.add(new Vehicle());
        }
        Vehicle circleTrain = TfL.vehicles.get(0);
        Vehicle shuttle = TfL.vehicles.get(1);
        circleTrain.speed = 0.1;
        circleTrain.route = new Edge []{
                TfL.edges.get(0),
                TfL.edges.get(1),
                TfL.edges.get(2),
                TfL.edges.get(3)
        };
        shuttle.speed = 0.5;
        shuttle.route = new Edge []{
                TfL.edges.get(4),
                TfL.edges.get(5)
        };
        TfL.edges.get(0).vehicles.add(circleTrain);
        TfL.edges.get(5).vehicles.add(shuttle);

        // ----------- Add contents
        for(i=0; i<3; ++i) {
            TfL.people.add(new Person());
        }
        Person me = TfL.people.get(0);
        Person dave = TfL.people.get(1);
        Person jon = TfL.people.get(2);
        tangTown.contents.add(me);
        culleyCorner.contents.add(dave);
        jonsBottom.contents.add(jon);

        // ----------- Run simulation
        for(i = 0; i < 1000; ++i) {
            TfL.step();
            System.out.println("Circle: "+circleTrain + " Shuttle :"+shuttle);
        }
    }
}
