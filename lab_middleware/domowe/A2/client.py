import logging
import threading

import time
import grpc
import sys

sys.path.append('generated')


from generated import server_pb2
from generated import server_pb2_grpc


def handle_grpc_errors(func):
    def wrapper(*args, **kwargs):
        while True:
            try:
                return func(*args, **kwargs)
            except grpc.RpcError as e:
                if e.code() == grpc.StatusCode.UNAVAILABLE:
                    print("Server is unavailable, trying to reconnect...")
                    time.sleep(5)
                else:
                    raise e

    return wrapper


@handle_grpc_errors
def print_all_events(stub):
    print("\n---ALL AVAILABLE EVENTS---\n")
    responses = stub.getAllEvents(server_pb2.Empty())
    for response in responses:
        print(
            f"Event id: {response.event_id}, name: {response.name}, type: {server_pb2.EventType.Name(response.type)}, date: {response.date}, location: {response.location}, max participants: {response.maxParticipants}")
        print("\n")


@handle_grpc_errors
def subscribe_to_event(stub, user_id, event_id):
    if not user_id.isdigit():
        print("[WARNING] Id must be a number")
        return
    response = stub.subscribeEvent(server_pb2.EventSubscriptionRequest(user_id=user_id, event_id=event_id))
    print(f"Event {response.event_id}: {response.name} has been subscribed!")


@handle_grpc_errors
def print_subscribed_events(stub, user_id):
    print("\n---SUBSCRIBED EVENTS---\n")
    responses = stub.getSubscribedEvents(server_pb2.UserId(user_id=user_id))
    for response in responses:
        print(f"Event id: {response.event_id}, name: {response.name}, date: {response.date}")


@handle_grpc_errors
def unsubscribe_from_event(stub, user_id, event_id):
    if not user_id.isdigit():
        print("[WARNING] Id must be a number")
        return
    response = stub.unsubscribeEvent(server_pb2.EventSubscriptionRequest(user_id=user_id, event_id=event_id))
    print(f"Event {response.event_id}: {response.name} has been unsubscribed!")


@handle_grpc_errors
def listen_for_updates(stub, user_id):
    responses = stub.notifySubscribers(server_pb2.UserId(user_id=user_id))
    for response in responses:
        print(f"Notification for event id: {response.event_id}, name: {response.name}, date: {response.date}")


@handle_grpc_errors
def console(stub, user_id):
    print(
        "q -> quit, show -> show all available events, sub -> subscribe event, subscribed -> show subscribed, unsub -> unsubscribe event")
    while True:
        user_input = input(":")
        if user_input == 'q':
            return
        if user_input == 'show':
            print_all_events(stub)
        if user_input == 'sub':
            print("Enter event id:")
            event_id = input(":")
            subscribe_to_event(stub, user_id, event_id)
        if user_input == 'subscribed':
            print_subscribed_events(stub, user_id)
        if user_input == 'unsub':
            print("Enter event id:")
            event_id = input(":")
            unsubscribe_from_event(stub, user_id, event_id)


@handle_grpc_errors
def run(stub):
    print("Client started")
    user_id = input("Enter user id:")
    threading.Thread(target=console, args=(stub, user_id,)).start()
    threading.Thread(target=listen_for_updates, args=(stub, user_id,)).start()


if __name__ == '__main__':
    logging.basicConfig()
    channel = grpc.insecure_channel("localhost:50051")
    stub = server_pb2_grpc.EventServiceStub(channel)
    run(stub)
