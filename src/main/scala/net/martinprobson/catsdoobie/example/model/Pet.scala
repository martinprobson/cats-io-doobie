package net.martinprobson.catsdoobie.example.model

import net.martinprobson.catsdoobie.example.model.PetWithId.ID

case class Pet(name: String)
case class PetWithId(id: ID, name: String)

object PetWithId {
  type ID = String
}
