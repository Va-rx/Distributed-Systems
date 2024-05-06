from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class EventType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    KIDS: _ClassVar[EventType]
    ADULTS: _ClassVar[EventType]
    SENIORS: _ClassVar[EventType]
KIDS: EventType
ADULTS: EventType
SENIORS: EventType

class Event(_message.Message):
    __slots__ = ("event_id", "name", "type", "date", "location", "maxParticipants")
    EVENT_ID_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    TYPE_FIELD_NUMBER: _ClassVar[int]
    DATE_FIELD_NUMBER: _ClassVar[int]
    LOCATION_FIELD_NUMBER: _ClassVar[int]
    MAXPARTICIPANTS_FIELD_NUMBER: _ClassVar[int]
    event_id: str
    name: str
    type: EventType
    date: str
    location: str
    maxParticipants: int
    def __init__(self, event_id: _Optional[str] = ..., name: _Optional[str] = ..., type: _Optional[_Union[EventType, str]] = ..., date: _Optional[str] = ..., location: _Optional[str] = ..., maxParticipants: _Optional[int] = ...) -> None: ...

class EventSubscriptionRequest(_message.Message):
    __slots__ = ("user_id", "event_id")
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    EVENT_ID_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    event_id: str
    def __init__(self, user_id: _Optional[str] = ..., event_id: _Optional[str] = ...) -> None: ...

class UserId(_message.Message):
    __slots__ = ("user_id",)
    USER_ID_FIELD_NUMBER: _ClassVar[int]
    user_id: str
    def __init__(self, user_id: _Optional[str] = ...) -> None: ...

class Empty(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...
