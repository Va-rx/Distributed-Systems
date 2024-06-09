package org.example;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class ZNodeMonitor implements Watcher {

    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
    static private ZooKeeper zooKeeper;
    private Process process;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ZNodeMonitor zNodeMonitor = new ZNodeMonitor();
        new Thread(zNodeMonitor::listenForConsoleInput).start();
        zNodeMonitor.connectToZookeeper();
        zNodeMonitor.watchZNode();
        zNodeMonitor.run();
    }

    public void connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
    }

    public void watchZNode() throws KeeperException, InterruptedException {
        zooKeeper.exists("/a", this);
    }

    public void run() throws InterruptedException {
        synchronized (this) {
            while (true) {
                this.wait();
            }
        }
    }

    public void listenForConsoleInput() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String input = scanner.nextLine();
                if ("tree".equals(input)) {
                    printNodeStructure("/a");
                }
            }
        }).start();
    }

    @Override
    public synchronized void process(WatchedEvent event) {
        synchronized (this) {
            switch (event.getType()) {
                case NodeCreated:
                    System.out.println("Node " + event.getPath() + " has been created");
                    try {
                        zooKeeper.getChildren(event.getPath(), true);
                    } catch (KeeperException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    if (event.getPath().equals("/a")) {
                        openBrowser();
                    }
                    break;

                case NodeDeleted:
                    System.out.println("Node " + event.getPath() + " has been deleted");
                    if (event.getPath().equals("/a")) {
                        closeBrowser();
                    }
                    break;

                case NodeChildrenChanged:
                    System.out.println("Node " + event.getPath() + " has been created");
                    try {
                        int n = zooKeeper.getChildren(event.getPath(), this).toArray().length;
                        System.out.println("Amount of children of /a: " + n );
                    } catch (KeeperException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    break;

                default:
                    break;
            }
            this.notifyAll();
        }
        try {
            watchZNode();
        } catch (KeeperException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private synchronized void printNodeStructure(String nodePath) {
        try {
            List<String> children = zooKeeper.getChildren(nodePath, false);

            System.out.println(nodePath);

            for (String child : children) {
                printNodeStructure(nodePath + "/" + child);
            }
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void openBrowser() {
        try {
            if (this.process == null || !this.process.isAlive()) {
                ProcessBuilder pb = new ProcessBuilder("firefox");
                this.process = pb.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeBrowser() {
        if (this.process.isAlive()) {
            System.out.println("Process firefox is alive, trying to destroy");
            this.process.destroyForcibly();
            System.out.println("Process firefox destroyed");
        } else {
            System.out.println("Process firefox is not alive");
        }
    }
}