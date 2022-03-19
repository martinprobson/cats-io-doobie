package net.martinprobson.catsdoobie.example.model

import net.martinprobson.catsdoobie.example.model.Pet.PET_ID

case class Pet(id: PET_ID, name: String, owner: Owner)

object Pet {

  def apply(id: PET_ID, name: String, owner: Owner): Pet = new Pet(id, name, owner)

  def apply(name: String, owner: Owner): Pet = new Pet(UNASSIGNED_PET_ID, name, owner)

  type PET_ID = String

  val UNASSIGNED_PET_ID = ""
}
