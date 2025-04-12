#[macro_use] extern crate rocket;

use graphql_client::GraphQLQuery;
use rocket::serde::json::Json;
use serde::{Deserialize};

#[derive(Deserialize)]
#[serde(crate = "rocket::serde")]
struct Task<'r> {
    description: &'r str,
    complete: bool
}

#[allow(clippy::upper_case_acronyms)]
type URI = String;
type DateTime = String;

#[derive(GraphQLQuery, Deserialize)]
#[graphql(
    schema_path = "app/src/main/graphql/schema.graphqls",
    query_path = "app/src/main/graphql/GetReleases.graphql",
    response_derives = "Serialize,PartialEq",
)]
struct GetReleases;

#[post("/graphql", data = "<query>")]
fn graphql(query: Json<Task<'_>>) -> String {
    format!("test {}", query.description)
}

#[launch]
fn rocket() -> _ {
    rocket::build().mount("/", routes![graphql])
}
