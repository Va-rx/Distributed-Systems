import enum
import time
import grpc
import logging
from concurrent import futures
import sys

sys.path.append('generated')

import generated.server_pb2 as server_pb2
import generated.server_pb2_grpc as server_pb2_grpc


class EventType(enum.Enum):
    KIDS = 0
    ADULTS = 1
    SENIORS = 2


users_subscription = {}


class Event:
    def __init__(self, id, name, type, date, location, maxParticipants):
        self.id = id
        self.name = name
        self.type = type
        self.date = date
        self.location = location
        self.maxParticipants = maxParticipants


events = [Event('1', "Event 1", EventType.KIDS, "2021-01-01", "Location 1", 100),
          Event('2', "Event 2", EventType.SENIORS, "2021-02-02", "Location 2", 200),
          Event('3', "Event 3", EventType.ADULTS, "2021-03-03", "Location 3", 300), ]


class EventService(server_pb2_grpc.EventServiceServicer):

    def getAllEvents(self, request, context):
        print("User is trying to get all events")
        for event in events:
            yield server_pb2.Event(event_id=event.id, name=event.name, type=event.type.value, date=event.date,
                                   location=event.location, maxParticipants=event.maxParticipants)

    def subscribeEvent(self, request, context):
        event_id = request.event_id
        user_id = request.user_id
        print(f"User {user_id} is trying to subscribe the event {event_id}")
        for event in events:
            if event.id == event_id:
                print(f"User {user_id} subscribed the event {event_id}")
                users_subscription.setdefault(user_id, []).append(event_id)
                return server_pb2.Event(event_id=event.id, name=event.name, type=event.type.value, date=event.date,
                                        location=event.location, maxParticipants=event.maxParticipants)

    def getSubscribedEvents(self, request, context):
        user_id = request.user_id
        print(f"User {user_id} is trying to get subscribed events")
        for event in events:
            if event.id in users_subscription.get(user_id, []):
                print(f"User {user_id} got subscribed event {event.id}")
                yield server_pb2.Event(event_id=event.id, name=event.name, type=event.type.value, date=event.date,
                                       location=event.location, maxParticipants=event.maxParticipants)

    def unsubscribeEvent(self, request, context):
        event_id = request.event_id
        user_id = request.user_id
        print(f"User {user_id} is trying to unsubscribe the event {event_id}")
        for event in events:
            if event.id == event_id:
                print(f"User {user_id} unsubscribed the event {event_id}")
                users_subscription.get(user_id, []).remove(event_id)
                return server_pb2.Event(event_id=event.id, name=event.name, type=event.type.value, date=event.date,
                                        location=event.location, maxParticipants=event.maxParticipants)

    def notifySubscribers(self, request, context):
        user_id = request.user_id
        while True:
            if user_id in users_subscription:
                for event_id in users_subscription[user_id]:
                    for event in events:
                        if event.id == event_id:
                            message = server_pb2.Event(event_id=event.id, name=event.name, type=event.type.value,
                                                       date=event.date,
                                                       location=event.location, maxParticipants=event.maxParticipants)
                            print(f"User {user_id} got notification for event {event_id}")
                            yield message
            time.sleep(10)


def serve():
    port = "50051"
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    server_pb2_grpc.add_EventServiceServicer_to_server(EventService(), server)
    server.add_insecure_port("[::]:" + port)
    server.start()
    print("Server started, listening on " + port)
    server.wait_for_termination()


if __name__ == '__main__':
    logging.basicConfig()
    serve()
