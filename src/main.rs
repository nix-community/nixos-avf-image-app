#[macro_use] extern crate rocket;


use graphql_client::{GraphQLQuery, Response};
use serde::{Deserialize, Serialize};
use rocket::serde::{json::Json};
use crate::get_releases::Variables;

#[allow(clippy::upper_case_acronyms)]
type URI = String;
type DateTime = String;

#[derive(Debug, Serialize, Deserialize, Clone)]
struct QueryBody<'a> {
    /// The values for the variables. They must match those declared in the queries.
    pub variables: EmptyVars,
    /// The GraphQL query, as a string.
    pub query: &'a str,
    /// The GraphQL operation name, as a string.
    #[serde(rename = "operationName")]
    pub operation_name: &'a str,
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

#[post("/graphql", data = "<query>")]
async fn graphql(query: Json<QueryBody<'_>>) -> Json<Response<GetReleases>> {
    let request_body = GetReleases::build_query(Variables {});

    // Send the query (e.g., using reqwest or another HTTP client)
    let response: Response<GetReleases> = reqwest::Client::new()
        .post("https://api.example.com/graphql")
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
    rocket::build().mount("/", routes![graphql])
}
