package test;

import io.fabric8.kubernetes.api.model.EndpointAddress;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.client.*;

import java.util.List;
import java.util.stream.Collectors;

import static okhttp3.TlsVersion.TLS_1_0;

/**
 * 2019-10-11 14:55
 *
 * @author marscong
 */
public class WatchClient {



    static String ns = "auth";

    static String serviceName = "authentication-service";

    public static void main(String[] args) {

        Config config = new ConfigBuilder()
                .withTrustCerts(true)
                .withTlsVersions(TLS_1_0)
                .withNamespace(ns)
                .build();

        KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);


        System.out.println("Server kubernetesClient: " + kubernetesClient.getVersion().getGitVersion());



        kubernetesClient.endpoints().inNamespace(ns)
                .withName(serviceName)
                .watch(new Watcher<>() {
                    @Override
                    public void eventReceived(Action action, Endpoints endpoints) {
                        switch (action) {
                            case MODIFIED:
                            case ADDED:
                                System.out.println(action +"  : " + dump(endpoints));
                                return;
                            case DELETED:
                                System.out.println("DELETED :" + endpoints);
                        }
                    }

                    @Override
                    public void onClose(KubernetesClientException e) {

                    }
                });

        while (true) {

            Endpoints endpoints = kubernetesClient.endpoints().inNamespace(ns)
                    .withName(serviceName)
                    .get();

            System.out.println("get : " +  dump(endpoints));

            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    static List<String> dump(Endpoints endpoints){

        var urls = endpoints.getSubsets().stream()
                .flatMap(sub->sub.getAddresses().stream())
                .map(EndpointAddress::getIp)
                .collect(Collectors.toList());

        return urls;
    }
}