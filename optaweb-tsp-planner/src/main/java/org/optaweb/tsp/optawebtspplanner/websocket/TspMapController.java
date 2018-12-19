/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.tsp.optawebtspplanner.websocket;

import org.optaweb.tsp.optawebtspplanner.core.LatLng;
import org.optaweb.tsp.optawebtspplanner.interactor.LocationInteractor;
import org.optaweb.tsp.optawebtspplanner.planner.RouteChangedEvent;
import org.optaweb.tsp.optawebtspplanner.planner.TspPlannerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

/**
 * Handles WebSocket subscriptions and STOMP messages.
 * @see WebSocketConfig
 */
@Controller
public class TspMapController {

    private static final Logger logger = LoggerFactory.getLogger(TspMapController.class);

    private final TspPlannerComponent planner;
    private final RoutePublisher routePublisher;
    private final LocationInteractor locationInteractor;

    @Autowired
    public TspMapController(TspPlannerComponent planner,
                            RoutePublisher routePublisher,
                            LocationInteractor locationInteractor) {
        this.planner = planner;
        this.routePublisher = routePublisher;
        this.locationInteractor = locationInteractor;
    }

    /**
     * Subscribe for updates of the TSP route.
     * @return route message
     */
    @SubscribeMapping("/route")
    public RouteMessage subscribe() {
        logger.info("Subscribed");
        RouteChangedEvent event = planner.getSolution();
        return routePublisher.createResponse(event.getDistance(), event.getRoute());
    }

    /**
     * Create new location.
     * @param request new location description
     */
    @MessageMapping("/place") // TODO rename to location
    public void create(PortableLocation request) {
        locationInteractor.addLocation(new LatLng(request.getLatitude(), request.getLongitude()));
    }

    /**
     * Load a demo consisting of a number of cities.
     */
    @MessageMapping("/demo")
    public void demo() {
        locationInteractor.loadDemo();
    }

    /**
     * Delete location.
     * @param id ID of the location to be deleted
     */
    @MessageMapping({"/place/{id}/delete"}) // TODO rename to location
    public void delete(@DestinationVariable Long id) {
        locationInteractor.removeLocation(id);
    }
}
