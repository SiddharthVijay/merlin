package com.novoda.merlin.service;

import com.novoda.merlin.Merlin;
import com.novoda.merlin.MerlinLog;
import com.novoda.merlin.service.request.MerlinRequest;

import static com.novoda.merlin.service.ResponseCodeValidator.DefaultEndpointResponseCodeValidator;

class HostPinger {

    private final PingerCallback pingerCallback;
    private final String hostAddress;
    private final PingTaskFactory pingTaskFactory;

    interface PingerCallback {

        void onSuccess();

        void onFailure();

    }

    public static HostPinger withDefaultEndpointValidation(PingerCallback pingerCallback) {
        MerlinLog.d("Host address not set, using Merlin default: " + Merlin.DEFAULT_ENDPOINT);
        PingTaskFactory pingTaskFactory = new PingTaskFactory(pingerCallback, new ResponseCodeFetcher(), new DefaultEndpointResponseCodeValidator());
        return new HostPinger(pingerCallback, Merlin.DEFAULT_ENDPOINT, pingTaskFactory);
    }

    public static HostPinger withCustomEndpointAndValidation(PingerCallback pingerCallback, String hostAddress, ResponseCodeValidator validator) {
        PingTaskFactory pingTaskFactory = new PingTaskFactory(pingerCallback, new ResponseCodeFetcher(), validator);
        return new HostPinger(pingerCallback, hostAddress, pingTaskFactory);
    }

    HostPinger(PingerCallback pingerCallback, String hostAddress, PingTaskFactory pingTaskFactory) {
        this.pingerCallback = pingerCallback;
        this.hostAddress = hostAddress;
        this.pingTaskFactory = pingTaskFactory;
    }

    public void ping() {
        PingTask pingTask = pingTaskFactory.create(hostAddress);
        pingTask.execute();
    }

    public void noNetworkToPing() {
        pingerCallback.onFailure();
    }

    public static class ResponseCodeFetcher {

        public int from(String endpoint) {
            return MerlinRequest.head(endpoint).getResponseCode();
        }

    }

}
