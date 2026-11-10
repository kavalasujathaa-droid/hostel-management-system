from pydantic import BaseModel, ConfigDict


class HealthLiveResponse(BaseModel):
    status: str
    app: str
    environment: str

    model_config = ConfigDict(extra="forbid")


class HealthReadyResponse(BaseModel):
    status: str
    database: str

    model_config = ConfigDict(extra="forbid")
