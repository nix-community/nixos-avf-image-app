#[macro_use] extern crate rocket;

use std::env;
use graphql_client::{GraphQLQuery, Response};
use serde::{Deserialize, Serialize};
use rocket::serde::{json::Json};
use rocket::State;
use crate::get_releases::{Variables, ResponseData};
use twelf::{config, Layer};

#[allow(clippy::upper_case_acronyms)]
type URI = String;
type DateTime = String;

#[derive(Debug, Serialize, Deserialize, Clone)]
struct QueryBody {
    /// The values for the variables. They must match those declared in the queries.
    pub variables: EmptyVars,
    /// The GraphQL query, as a string.
    pub query: String,
    /// The GraphQL operation name, as a string.
    #[serde(rename = "operationName")]
    pub operation_name: String,
}

/*

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct QueryBody<'a, Variables> {
    /// The values for the variables. They must match those declared in the queries.
    pub variables: Variables,
    /// The GraphQL query, as a string.
    pub query: &'a str,
    /// The GraphQL operation name, as a string.
    #[serde(rename = "operationName")]
    pub operation_name: &'a str,
}

 */

#[derive(Debug, Serialize, Deserialize, Clone)]
struct EmptyVars {

}

#[derive(GraphQLQuery, Serialize, Deserialize, Clone)]
#[graphql(
    schema_path = "app/src/main/graphql/schema.graphqls",
    query_path = "app/src/main/graphql/GetReleases.graphql",
    response_derives = "Serialize,PartialEq",
)]
struct GetReleases;

#[config]
#[derive(Debug, Default, Serialize, Clone)]
pub struct Config {
    pub port: u16,
    pub token: String,
}

pub fn loadConfig() -> Config {
    // Layer from different sources to build configuration. Order matters!
    let conf = Config::with_layers(&[
        Layer::Yaml(env::current_dir().expect("Failed to get CWD").join("config.yaml")),
        Layer::Env(Some(String::from("APP_"))),
    ]).expect("Failed to load config");

    conf
}

#[post("/graphql", data = "<query>")]
async fn graphql(query: Json<QueryBody>, config: &State<Config>) -> Json<Response<ResponseData>> {
    let request_body = GetReleases::build_query(Variables {});

    let response: Response<ResponseData> = reqwest::Client::new()
        .post("https://api.github.com/graphql")
        .header(reqwest::header::USER_AGENT, "github.com/mkg20001/nixos-avf-image-app")
        .bearer_auth(config.token.clone())
        .json(&request_body)
        .send()
        .await
        .expect("Failed to send GraphQL request")
        .json()
        .await
        .expect("Failed to parse GraphQL response");

    Json(response)
}

#[launch]
fn rocket() -> _ {
    let config = loadConfig();
    rocket::build()
        .configure(rocket::Config::figment().merge(("port", config.port)).merge(("address", "::")))
        .manage(config)
        .mount("/", routes![graphql])
}
