package org.jenkinsci.plugins.koji;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.MyTypeFactory;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Singleton used for XML-RPC communication with Koji.
 */
public class KojiClient {

    private XmlRpcClient koji;
    private static KojiClient instance;
    private String kojiInstanceURL;

    public String getKojiInstanceURL() {
        return kojiInstanceURL;
    }

    private KojiClient(String kojiInstanceURL) {
        this.kojiInstanceURL = kojiInstanceURL;
        this.koji = connect(kojiInstanceURL);
    }

    /**
     * Get the KojiClient Singleton instance.
     * @param kojiInstanceURL URL of remote Koji instance.
     */
    public static KojiClient getKojiClient(String kojiInstanceURL) {
        if (instance == null)
            instance = new KojiClient(kojiInstanceURL);

        return instance;
    }

    /**
     * Gets latest builds.
     * @param tag Koji tag
     * @param pkg Koji package
     * @return Array of properties for latest build.
     */
    public Object[] getLatestBuilds(String tag, String pkg) {
        // Koji XML-RPC API
        // getLatestBuilds(tag, event=None, package=None, type=None)
        // description: List latest builds for tag (inheritance enabled)

        List<Object> params = new ArrayList<Object>();
        params.add(tag);
        // Event is of no interest to us.
        params.add(null);
        params.add(pkg);
        try {
            Object[] latestBuilds = (Object[]) koji.execute("getLatestBuilds", params);

            return latestBuilds;
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves information about a given build.
     * @param buildId BuildId can be Name-Version-Release (NVR) or numeric buildId.
     * @return A map containing all information about given build.
     */
    public Map<String, String> getBuildInfo(String buildId) {
     /* XML-RPC method information
        getBuild(buildInfo, strict=False)
        description: Return information about a build.  buildID may be either
        a int ID, a string NVR, or a map containing 'name', 'version'
        and 'release.  A map will be returned containing the following
        keys:
        id: build ID
        package_id: ID of the package built
        package_name: name of the package built
        version
        release
        epoch
        nvr
        state
        task_id: ID of the task that kicked off the build
        owner_id: ID of the user who kicked off the build
        owner_name: name of the user who kicked off the build
        volume_id: ID of the storage volume
        volume_name: name of the storage volume
        creation_event_id: id of the create_event
        creation_time: time the build was created (text)
        creation_ts: time the build was created (epoch)
        completion_time: time the build was completed (may be null)
        completion_ts: time the build was completed (epoch, may be null)

        If there is no build matching the buildInfo given, and strict is specified,
                raise an error.  Otherwise return None. */

        List<Object> params = new ArrayList<Object>();
        params.add(buildId);
        try {
            Map<String, String> buildInfo = (Map<String, String>) koji.execute("getBuild", params);

            return buildInfo;
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Gets information about logged user.
     * @return Session identification.
     */
    public String getSession() {
        try {
            String result = (String) koji.execute("showSession", new Object[] {});

            System.out.println(result);

            return result;
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }

        return null;
    }
     * Greet the remote Koji instance and test the communication.
     * @return
     */
    public String sayHello() {
        StringBuilder sb = new StringBuilder();
        try {
            Object result = koji.execute("hello", new Object[] {"Hello"});
            sb.append("Jenkins-Koji Plugin: Hello Koji server running at " + kojiInstanceURL);
            sb.append("\nKoji: " + result);

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Connect to remote Koji instance. Uses custom Transport factory adding a None / null support for XML-RPC.
     * @param kojiInstanceURL Address of the remote Koji server.
     * @return XMLRPC client instance.
     */
    private XmlRpcClient connect(String kojiInstanceURL) {
        XmlRpcClient koji = new XmlRpcClient();
        koji.setTransportFactory(new XmlRpcCommonsTransportFactory(koji));
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        koji.setTransportFactory(new XmlRpcCommonsTransportFactory(koji));
        koji.setTypeFactory(new MyTypeFactory(koji));
        config.setEnabledForExtensions(true);
        config.setEnabledForExceptions(true);

        try {
            config.setServerURL(new URL(kojiInstanceURL));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        koji.setConfig(config);

        return koji;
    }
}
